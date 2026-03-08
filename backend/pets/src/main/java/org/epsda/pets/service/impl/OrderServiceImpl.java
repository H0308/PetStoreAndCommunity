package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.tomcat.util.bcel.Const;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.OrderService;
import org.epsda.pets.service.ProfileService;
import org.epsda.pets.utils.BaiduMapUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.epsda.pets.utils.ShortestDistanceOnEarthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 16:06
 *
 * @Author: 憨八嘎
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private BaiduMapUtil baiduMapUtil;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private RefundInfoMapper refundInfoMapper;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Override
    public PageVO<OrderDetailVO> list(OrdersDetailDTO ordersDetailDTO, OrderFilterDTO orderFilterDTO) {
        if (ordersDetailDTO == null) {
            throw new PetException("订单信息错误，获取订单失败");
        }
        Long userId = ordersDetailDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 构建分页对象
        Long currentPage = ordersDetailDTO.getCurrentPage();
        Long pageSize = ordersDetailDTO.getPageSize();
        Page<Order> orderPage = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = this.buildPageFilterCondition(orderFilterDTO);
        orderLambdaQueryWrapper.eq(Order::getUserId, userId);

        Page<Order> orders = orderMapper.selectPage(orderPage, orderLambdaQueryWrapper);
        List<OrderDetailVO> orderDetailVOS = new ArrayList<>();

        List<Order> records = orders.getRecords();
        records.forEach(order -> orderDetailVOS.add(this.buildOrderDetailVOFromOrder(order)));

        return PageVO.<OrderDetailVO>builder()
                .currentPage(orders.getCurrent())
                .totalPages(orders.getPages())
                .totalCount(orders.getTotal())
                .totalRecords(orderDetailVOS)
                .build();
    }

    @Override
    public PreOrderDetailVO preCreate(OrderPreCreateDTO orderPreCreateDTO) {
        // 预创建订单信息：返回用户默认的信息
        // 预创建不处理修改订单信息（包括地址和数量），实际创建订单时再一次进行数量校验
        Long userId = orderPreCreateDTO.getUserId();
        Long productId = orderPreCreateDTO.getProductId();
        Long totalCount = orderPreCreateDTO.getTotalCount();
        if (userId == 0 || productId == 0 || totalCount == 0) {
            throw new PetException("订单信息错误，预创建订单失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("当前用户不存在，无法预创建订单");
        }
        if (!StringUtils.hasText(user.getRealName()) || !StringUtils.hasText(user.getIdCard())) {
            throw new PetException("用户未进行实名认证，无法预创建订单");
        }
        ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .and(productSuperQueryWrapper -> productSuperQueryWrapper
                        .eq(ProductSuper::getStatus, Constants.ON_SELLING)
                        .or()
                        .eq(ProductSuper::getStatus, Constants.SOLD_OUT)));
        if (productSuper == null) {
            throw new PetException("当前商品不存在或已下架，无法预创建订单");
        }

        Long receiptId = user.getReceiptId();
        String phone = user.getPhone();
        BigDecimal price = productSuper.getPrice();
        ProductImage productImage = productImageMapper.selectImagesByProductId(productId).get(0);
        Address address = addressMapper.selectById(receiptId);
        if (!StringUtils.hasText(phone)) {
            throw new PetException("当前账户没有绑定手机号，无法下单");
        }
        if (address == null) {
            throw new PetException("当前账户没有绑定收货地址，无法下单");
        }

        // 预创建订单不需要更改数据库，只返回动态计算的值
        Long stock = productSuper.getStock();
        if (stock.compareTo(totalCount) < 0) {
            throw new PetException("当前库存量不足，无法购买当前商品");
        }
        BigDecimal totalPrice = this.calculateTotalPrice(price, totalCount);

        return PreOrderDetailVO.builder().userId(userId).productType(productSuper.getType())
                .productId(productId).productName(productSuper.getName())
                .imgUrl(productImage.getImageUrl()).totalCount(totalCount)
                .totalPrice(totalPrice).receiptId(receiptId)
                .receiptName(user.getReceiptName()).phone(phone)
                .receiptAddress(address.getAddressText())
                .build();
    }

    @Transactional
    @Override
    public OrderDetailVO create(OrderConfirmDetailDTO orderConfirmDetailDTO) {
        // 实际创建订单
        Long userId = orderConfirmDetailDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long productId = orderConfirmDetailDTO.getProductId();
        // 因为收货人名称可能存在修改，所以需要从确认订单请求中拿到收货人名称
        String receiptName = orderConfirmDetailDTO.getReceiptName();
        // 因为快递地址可能存在修改，所以需要从确认订单请求中拿到地址信息
        Long receiptId = orderConfirmDetailDTO.getReceiptId();
        // 因为电话可能存在修改，所以需要从确认订单请求中拿到电话
        String phone = orderConfirmDetailDTO.getPhone();
        Long totalCount = orderConfirmDetailDTO.getTotalCount();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，创建订单失败");
        }
        ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .and(productSuperQueryWrapper -> productSuperQueryWrapper
                        .eq(ProductSuper::getStatus, Constants.ON_SELLING)
                        .or()
                        .eq(ProductSuper::getStatus, Constants.SOLD_OUT)));
        BigDecimal price = productSuper.getPrice();
        Address address = addressMapper.selectById(receiptId);
        ProductImage productImage = productImageMapper.selectImagesByProductId(productId).get(0);
        Long stock = productSuper.getStock();
        if (stock.compareTo(totalCount) < 0) {
            throw new PetException("当前库存量不足，无法购买当前商品");
        }
        BigDecimal totalPrice = this.calculateTotalPrice(price, totalCount);

        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setReceiptId(receiptId);
        order.setReceiptName(receiptName);
        order.setPhone(phone);
        order.setTotalCount(totalCount);
        order.setTotalPrice(totalPrice);

        boolean insertRet = orderMapper.insert(order) == 1;
        Long orderId = 0L;
        if (insertRet) {
            orderId = order.getId();
        }

        // 更新库存（锁库存）
        this.reduceProductStockAndStatus(productSuper, totalCount);

        if (orderId == 0L) {
            throw new PetException("订单ID错误，订单创建失败");
        }

        return OrderDetailVO.builder().orderId(orderId).userId(userId)
                .productId(productId).productName(productSuper.getName())
                .imgUrl(productImage.getImageUrl()).totalCount(totalCount)
                .totalPrice(totalPrice).receiptId(receiptId)
                .receiptName(receiptName).phone(phone)
                .receiptAddress(address.getAddressText())
                .createTime(order.getCreateTime())
                .status(Constants.ORDER_TO_PAY)
                .build();
    }

    @Transactional
    @Override
    public List<OrderDetailVO> batchCreate(List<OrderConfirmDetailDTO> orderConfirmDetailDTOs) {
        // 批量创建订单
        if (orderConfirmDetailDTOs == null || orderConfirmDetailDTOs.isEmpty()) {
            throw new PetException("订单信息不足，无法创建订单");
        }

        return orderConfirmDetailDTOs.stream().map(orderConfirmDetailDTO -> {
            OrderDetailVO orderDetailVO = null;
            try {
                orderDetailVO = this.create(orderConfirmDetailDTO);
            } catch (PetException petException) {
                return null;
            }

            return orderDetailVO;
        }).toList();
    }

    @Transactional
    @Override
    public ChangeOrderUserInfoVO changeUserInfo(OrderChangeUserInfoDTO orderChangeDTO) {
        Long userId = orderChangeDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        String receiptName = orderChangeDTO.getReceiptName();
        Long receiptId = orderChangeDTO.getReceiptId();
        String receiptAddress = orderChangeDTO.getReceiptAddress();
        String phone = orderChangeDTO.getPhone();

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Long oldReceiptId = user.getReceiptId();

        Long newReceiptId = 0L;
        // 判断是否修改了地址
        if (receiptId == null || (!oldReceiptId.equals(receiptId) && StringUtils.hasText(receiptAddress))) {
            // 地址存在修改，直接插入地址信息
            Address address = new Address();

            address.setAddressText(receiptAddress);
            String mapJson = baiduMapUtil.geographyEncoding(receiptAddress);
            List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
            address.setLatitude(latitudeAndLongitude.get(0));
            address.setLongitude(latitudeAndLongitude.get(1));
            boolean insertRet = addressMapper.insert(address) == 1;
            if (insertRet) {
                newReceiptId = address.getId();
            }
        }

        // 判断是否修改电话
        String newPhone = "";
        if (StringUtils.hasText(phone)) {
            // 电话存在修改，返回为新的电话信息
            newPhone = phone;
        }

        ChangeOrderUserInfoVO changeOrderUserInfoVO = new ChangeOrderUserInfoVO();
        if (newReceiptId > 0) {
            changeOrderUserInfoVO.setReceiptId(newReceiptId);
            changeOrderUserInfoVO.setReceiptAddress(receiptAddress);
        }

        if (!StringUtils.hasText(newPhone)) {
            changeOrderUserInfoVO.setPhone(newPhone);
        }

        // 判断是否修改了收货人名称
        if (StringUtils.hasText(receiptName)) {
            changeOrderUserInfoVO.setReceiptName(receiptName);
        }

        return changeOrderUserInfoVO;
    }

    @Override
    public ChangeOrderProductCountVO changeProductCount(OrderChangeProductCountDTO orderChangeProductCountDTO) {
        Long newCount = orderChangeProductCountDTO.getNewCount();
        Long productId = orderChangeProductCountDTO.getProductId();
        ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        ChangeOrderProductCountVO changeOrderProductCountVO = new ChangeOrderProductCountVO();
        // 设置默认值为-1用于前端校验是否修改成功
        changeOrderProductCountVO.setTotalCount(-1L);
        // 判断是否修改商品数量
        if (newCount != null) {
            // 商品数量发生改变，重新计算库存是否足够
            Long stock = productSuper.getStock();
            if (newCount <= 0) {
                throw new PetException("商品最小数量为1");
            }
            if (stock.compareTo(newCount) < 0 || productSuper.getStatus().equals(Constants.OFF_SHELF)) {
                throw new PetException("当前库存量不足或者商品已下架，无法购买当前商品");
            }
            BigDecimal price = productSuper.getPrice();
            // 库存足够，可以修改
            changeOrderProductCountVO.setTotalCount(newCount);
            BigDecimal totalPrice = price.multiply(new BigDecimal(newCount));
            changeOrderProductCountVO.setTotalPrice(totalPrice);
        }

        return changeOrderProductCountVO;
    }

    @Override
    public OrderDetailVO getOne(Long orderId, Long userId) {
        // 参数的userId用于权限校验，不用于查询
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        return this.buildOrderDetailVOFromOrder(orderMapper.selectById(orderId));
    }

    @Override
    public PayOrderVO pay(PayOrderDTO payOrderDTO) {
        // userId用于后期权限校验
        if (payOrderDTO.getOrderId() == 0 || payOrderDTO.getUserId() == 0) {
            throw new PetException("订单信息错误，无法支付");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(payOrderDTO.getUserId());

        Long orderId = payOrderDTO.getOrderId();
        // 获取订单状态
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new PetException("订单获取失败，无法支付");
        }
        Integer status = order.getStatus();
        // 只有处于待支付的订单才可以支付
        if (!status.equals(Constants.ORDER_TO_PAY)) {
            throw new PetException("订单状态错误，无法支付");
        }

        // 当前通过修改订单状态模拟支付
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(Constants.ORDER_TO_DELIVER);

        boolean updateRet = orderMapper.updateById(newOrder) == 1;
        PayOrderVO payOrderVO = new PayOrderVO();
        payOrderVO.setOrderId(-1L);
        payOrderVO.setSuccessFlag(false);
        if (updateRet) {
            payOrderVO.setOrderId(orderId);
            payOrderVO.setSuccessFlag(true);
        }

        return payOrderVO;
    }

    @Override
    public List<PayOrderVO> batchPay(List<PayOrderDTO> payOrderDTOs) {
        if (payOrderDTOs == null || payOrderDTOs.isEmpty()) {
            throw new PetException("订单信息不足，支付失败");
        }

        return payOrderDTOs.stream().map(payOrderDTO -> {
            PayOrderVO payOrderVO = null;
            try {
                payOrderVO = this.pay(payOrderDTO);
            } catch (PetException petException) {
                return null;
            }

            return payOrderVO;
        }).toList();
    }

    @Override
    public Boolean cancel(CancelOrderDTO cancelOrderDTO) {
        // userId用于后期权限校验
        if (cancelOrderDTO.getOrderId() == 0 || cancelOrderDTO.getUserId() == 0) {
            throw new PetException("订单信息错误，无法取消");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(cancelOrderDTO.getUserId());

        // 获取订单状态
        Long orderId = cancelOrderDTO.getOrderId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new PetException("订单获取失败，无法取消");
        }
        Integer status = order.getStatus();
        // 只有处于待支付的账单才可以取消
        if (!status.equals(Constants.ORDER_TO_PAY)) {
            throw new PetException("订单状态错误，无法取消");
        }

        // 修改订单状态并回滚商品数量
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(Constants.ORDER_CANCELLED);
        ProductSuper productSuper = productSuperMapper.selectById(order.getProductId());
        if (productSuper == null) {
            throw new PetException("商品获取错误，订单取消失败");
        }
        this.rollbackProductStockAndStatus(productSuper, order.getTotalCount());

        return orderMapper.updateById(newOrder) == 1;
    }

    @Override
    public List<Boolean> batchCancel(List<CancelOrderDTO> cancelOrderDTOs) {
        if (cancelOrderDTOs == null || cancelOrderDTOs.isEmpty()) {
            throw new PetException("订单信息不足，支付失败");
        }

        return cancelOrderDTOs.stream().map(cancelOrderDTO -> {
            try {
                this.cancel(cancelOrderDTO);
            } catch (PetException petException) {
                return false;
            }

            return true;
        }).toList();
    }

    @Transactional
    @Override
    public Boolean refund(RefundDTO refundDTO) {
        if (refundDTO == null) {
            throw new PetException("退款信息错误，无法退款");
        }

        Long orderId = refundDTO.getOrderId();
        Long userId = refundDTO.getUserId();
        String message = refundDTO.getMessage();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus().equals(Constants.ORDER_TO_PAY) ||
            order.getStatus().equals(Constants.ORDER_CANCELLED)) {
            throw new PetException("订单不存在或者订单状态处于待支付，无法退款");
        }

        if (!order.getUserId().equals(userId)) {
            throw new PetException("订单归属不正确，退款失败");
        }

        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderId(orderId);
        refundInfo.setUserId(userId);
        refundInfo.setMessage(message);

        boolean insertRet = refundInfoMapper.insert(refundInfo) == 1;
        if (!insertRet) {
            throw new PetException("订单退款信息新增失败");
        }
        Order refund = new Order();
        refund.setId(orderId);
        refund.setRefundFlag(Constants.ORDER_TO_REFUND);
        boolean updateRet = orderMapper.updateById(refund) == 1;
        if (!updateRet) {
            throw new PetException("订单状态更新错误");
        }

        return true;
    }

    @Transactional
    @Override
    public Boolean receive(ReceiveOrderDTO receiveOrderDTO) {
        if (receiveOrderDTO == null) {
            throw new PetException("订单信息错误，无法签收商品");
        }

        Long orderId = receiveOrderDTO.getOrderId();
        Long userId = receiveOrderDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new PetException("订单不存在，无法签收");
        }
        if (!order.getUserId().equals(userId)) {
            throw new PetException("订单归属不正确，签收失败");
        }
        // 只有状态为待签收且没有退款的商品才可以签收
        if (order.getStatus().equals(Constants.ORDER_TO_SIGN) &&
            order.getRefundFlag().equals(Constants.ORDER_NOT_REFUND)) {
            Order receive = new Order();
            receive.setId(orderId);
            receive.setStatus(Constants.ORDER_SIGNED);
            boolean updateRet = orderMapper.updateById(receive) == 1;
            if (!updateRet) {
                throw new PetException("订单更新失败");
            }
            // 删除物流信息
            OrderLogistics orderLogistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                    .eq(OrderLogistics::getOrderId, orderId)
                    .eq(OrderLogistics::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (orderLogistics == null) {
                throw new PetException("物流信息不存在，收货失败");
            }
            OrderLogistics received = new OrderLogistics();
            received.setId(orderLogistics.getId());
            received.setDeleteFlag(Constants.DELETED_FLAG);
            updateRet = orderLogisticsMapper.updateById(received) == 1;
            if (!updateRet) {
                throw new PetException("物流信息更新失败");
            }
        } else {
            throw new PetException("请在商品到达收货地点时再收货");
        }

        return true;
    }

    @Transactional
    @Override
    public Boolean cancelRefund(CancelRefundDTO cancelRefundDTO) {
        if (cancelRefundDTO == null) {
            throw new PetException("订单信息错误，无法取消退款");
        }

        Long orderId = cancelRefundDTO.getOrderId();
        Long userId = cancelRefundDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new PetException("订单不存在，无法取消退款");
        }
        if (!order.getUserId().equals(userId)) {
            throw new PetException("订单归属不正确，取消退款失败");
        }
        // 只有处于退款状态的订单才可以取消退款
        if (!order.getRefundFlag().equals(Constants.ORDER_TO_REFUND)) {
            throw new PetException("当前订单并不是退款中的状态，无法退款");
        }

        // 删除已有的退款信息再更新退款状态
        RefundInfo refundInfo = refundInfoMapper.selectOne(new LambdaQueryWrapper<RefundInfo>()
                .eq(RefundInfo::getOrderId, orderId)
                .eq(RefundInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (refundInfo == null) {
            throw new PetException("退款信息不存在，取消退款失败");
        }

        boolean refundUpdateRet = refundInfoMapper.update(new LambdaUpdateWrapper<RefundInfo>()
                .eq(RefundInfo::getId, refundInfo.getId())
                .set(RefundInfo::getDeleteFlag, Constants.DELETED_FLAG)) == 1;
        if (!refundUpdateRet) {
            throw new PetException("退款信息更新失败");
        }

        boolean orderUpdateRet = orderMapper.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getRefundFlag, Constants.ORDER_NOT_REFUND)) == 1;
        if (!orderUpdateRet) {
            throw new PetException("订单信息更新失败");
        }

        return true;
    }

    @Override
    public TransportVO logistics(TransportDTO transportDTO) {
        if (transportDTO == null) {
            throw new PetException("订单信息错误，获取物流信息失败");
        }

        Long orderId = transportDTO.getOrderId();

        OrderLogistics orderLogistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getOrderId, orderId)
                .eq(OrderLogistics::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (orderLogistics == null) {
            throw new PetException("指定物流信息不存在");
        }

        // 获取订单状态
        Order order = orderMapper.selectById(orderId);
        Integer orderStatus = order != null ? order.getStatus() : null;

        return TransportVO.builder()
                .logisticsId(orderLogistics.getId())
                .transportType(orderLogistics.getTransportType())
                .orderStatus(orderStatus)
                .originLat(orderLogistics.getOriginLat()).originLng(orderLogistics.getOriginLng())
                .destLat(orderLogistics.getDestLat()).destLng(orderLogistics.getDestLng())
                .currentLat(orderLogistics.getCurrLat()).currentLng(orderLogistics.getCurrLng())
                .build();
    }

    @Transactional
    @Override
    public TransportArrivedVO checkDelivered(CurrentTransportDTO currentTransportDTO) {
        // 注意当前接口，管理员和用户如果同时判断成功抵达，可能会有并发安全问题，待修复
        if (currentTransportDTO == null) {
            throw new PetException("地址信息错误，判断收货状态失败");
        }

        Long logisticsId = currentTransportDTO.getLogisticsId();
        String currentLat = currentTransportDTO.getCurrentLat();
        String currentLng = currentTransportDTO.getCurrentLng();

        OrderLogistics orderLogistics = orderLogisticsMapper.selectOne(new LambdaQueryWrapper<OrderLogistics>()
                .eq(OrderLogistics::getId, logisticsId)
                .eq(OrderLogistics::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (orderLogistics == null) {
            throw new PetException("物流信息不存在或者物品已经送达");
        }

        String destLat = orderLogistics.getDestLat();
        String destLng = orderLogistics.getDestLng();

        // 先判断当前地址是否已经到达目的地址附近
        boolean isNear = ShortestDistanceOnEarthUtil.isWithinThreshold(currentLat, currentLng,
                destLat, destLng, Constants.DIS_THRESHOLD);
        if (isNear) {
            // 修改订单状态为已到达
            Long orderId = orderLogistics.getOrderId();
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new PetException("订单不存在，修改订单失败");
            }
            boolean updateRet = orderMapper.update(new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, orderId)
                    .set(Order::getStatus, Constants.ORDER_TO_SIGN)) == 1;
            if (!updateRet) {
                throw new PetException("订单状态更新失败");
            }
        }

        // 再存储当前地址
        boolean logisticsUpdateRet = orderLogisticsMapper.update(new LambdaUpdateWrapper<OrderLogistics>()
                .eq(OrderLogistics::getId, logisticsId)
                .set(OrderLogistics::getCurrLat, currentLat)
                .set(OrderLogistics::getCurrLng, currentLng)) == 1;

        if (!logisticsUpdateRet) {
            throw new PetException("当前物流地址更新失败");
        }

        return TransportArrivedVO.builder()
                .isArrived(isNear).successFlag(true)
                .build();
    }

    @Override
    public Boolean save(SaveOrderUserInfoDTO saveOrderUserInfoDTO) {
        if (saveOrderUserInfoDTO == null || saveOrderUserInfoDTO.getUserId() == null) {
            throw new PetException("当前用户ID错误，无法保存");
        }

        Long userId = saveOrderUserInfoDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("当前用户不存在，无法保存");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        String receiptName = saveOrderUserInfoDTO.getReceiptName();
        String phone = saveOrderUserInfoDTO.getPhone();
        Long receiptId = saveOrderUserInfoDTO.getReceiptId();

        boolean phoneRet = true;
        if (StringUtils.hasText(phone)) {
            if (!user.getPhone().equals(phone)) {
                String ret = profileService.changePhone(userId, phone);
                if (!StringUtils.hasText(ret)) {
                    phoneRet = false;
                }
            }
        }

        boolean receiptNameRet = true;
        if (StringUtils.hasText(receiptName)) {
            if (!user.getReceiptName().equals(receiptName)) {
                User newUser = new User();
                newUser.setId(userId);
                newUser.setReceiptName(receiptName);
                receiptNameRet = userMapper.updateById(newUser) == 1;
            }
        }

        boolean receiptIdRet = true;
        if (receiptId != null) {
            if (!user.getReceiptId().equals(receiptId)) {
                User newUser = new User();
                newUser.setId(userId);
                newUser.setReceiptId(receiptId);
                receiptIdRet = userMapper.updateById(newUser) == 1;
            }
        }

        return phoneRet && receiptNameRet && receiptIdRet;
    }

    public LambdaQueryWrapper<Order> buildPageFilterCondition(OrderFilterDTO orderFilterDTO) {
        if (orderFilterDTO == null) {
            return null;
        }
        Integer latest = orderFilterDTO.getLatest();
        Integer status = orderFilterDTO.getStatus();
        LocalDateTime startTime = orderFilterDTO.getStartTime();
        LocalDateTime endTime = orderFilterDTO.getEndTime();
        Integer refundFlag = orderFilterDTO.getRefundFlag();

        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 按照时间升序/降序排序
        if (latest != null) {
            if (latest == 1) {
                orderLambdaQueryWrapper.orderByDesc(Order::getUpdateTime); // 降序
            } else {
                orderLambdaQueryWrapper.orderByAsc(Order::getUpdateTime); // 升序
            }
        }

        // 按照订单状态分类
        if (status != null) {
            orderLambdaQueryWrapper.eq(Order::getStatus, status)
                    .eq(Order::getRefundFlag, Constants.ORDER_NOT_REFUND);
        }

        // 按照退款分类筛选
        if (refundFlag != null && refundFlag.equals(Constants.ORDER_TO_REFUND)) {
            orderLambdaQueryWrapper.eq(Order::getRefundFlag, Constants.ORDER_TO_REFUND);
        }

        // 按照时间段筛选
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new PetException("开始时间不允许大于结束时间");
            }
            orderLambdaQueryWrapper.between(Order::getCreateTime, startTime, endTime);
        }

        return orderLambdaQueryWrapper;
    }

    // 根据已有的订单记录构建确定的订单信息实体
    public OrderDetailVO buildOrderDetailVOFromOrder(Order order) {
        Long orderId = order.getId();
        Long userId = order.getUserId();
        Long productId = order.getProductId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (user == null) {
            throw new PetException("用户不存在，获取订单失败");
        }

        Long receiptId = order.getReceiptId();
        ProductSuper productSuper = productSuperMapper.selectById(productId);
        ProductImage productImage = productImageMapper.selectImagesByProductId(productId).get(0);
        Address address = addressMapper.selectById(receiptId);

        return OrderDetailVO.builder().orderId(orderId).userId(userId)
                .productId(productId).productName(productSuper.getName()).productType(productSuper.getType())
                .imgUrl(productImage.getImageUrl()).totalCount(order.getTotalCount())
                .totalPrice(order.getTotalPrice()).receiptId(receiptId)
                .receiptName(order.getReceiptName()).phone(order.getPhone())
                .receiptAddress(address.getAddressText())
                .createTime(order.getCreateTime()).status(order.getStatus())
                .refundFlag(order.getRefundFlag())
                .build();
    }

    public BigDecimal calculateTotalPrice(BigDecimal price, Long totalCount) {
        return price.multiply(new BigDecimal(totalCount));
    }

    public void reduceProductStockAndStatus(ProductSuper productSuper, Long totalCount) {
        ProductSuper stockProduct = new ProductSuper();
        Long stock = productSuper.getStock();
        if (stock <= 0 || stock.compareTo(totalCount) < 0|| productSuper.getStatus().equals(Constants.OFF_SHELF)) {
            throw new PetException("当前库存量不足或者商品已下架，无法购买当前商品");
        }
        stockProduct.setId(productSuper.getId());
        long rest = stock - totalCount;
        if (rest == 0) {
            // 更新商品状态
            ProductSuper newProductSuper = new ProductSuper();
            newProductSuper.setId(productSuper.getId());
            newProductSuper.setStatus(Constants.SOLD_OUT);
            productSuperMapper.updateById(newProductSuper);
        }
        stockProduct.setStock(rest);
        boolean updateRet = productSuperMapper.updateById(stockProduct) == 1;
        if (!updateRet) {
            throw new PetException("更新库存失败");
        }
    }

    public void rollbackProductStockAndStatus(ProductSuper productSuper, Long totalCount) {
        ProductSuper stockProduct = new ProductSuper();
        Long stock = productSuper.getStock();
        stockProduct.setId(productSuper.getId());
        long rest = stock + totalCount;
        if (rest > 0) {
            // 更新商品状态
            ProductSuper newProductSuper = new ProductSuper();
            newProductSuper.setId(productSuper.getId());
            newProductSuper.setStatus(Constants.ON_SELLING);
            productSuperMapper.updateById(newProductSuper);
        }
        stockProduct.setStock(rest);
        boolean updateRet = productSuperMapper.updateById(stockProduct) == 1;
        if (!updateRet) {
            throw new PetException("更新库存失败");
        }
    }
}
