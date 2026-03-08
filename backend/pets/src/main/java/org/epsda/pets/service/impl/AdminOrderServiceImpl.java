package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.ExcelOrderBO;
import org.epsda.pets.pojo.dto.AdminOrderChangeDTO;
import org.epsda.pets.pojo.dto.HandleRefundDTO;
import org.epsda.pets.pojo.dto.OrderListDTO;
import org.epsda.pets.pojo.dto.OrderListFilterDTO;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.AdminOrderService;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.utils.BaiduMapUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/03
 * Time: 21:36
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private BaiduMapUtil baiduMapUtil;
    @Autowired
    private RefundInfoMapper refundInfoMapper;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;
    @Autowired
    private MessagePushService messagePushService;

    @Override
    public PageVO<OrderListVO> list(OrderListDTO orderListDTO, OrderListFilterDTO orderListFilterDTO) {
        if (orderListDTO == null) {
            throw new PetException("订单信息缺失，无法获取订单列表");
        }

        SecurityUtil.checkAdmin(orderListDTO.getUserId());
        Long currentPage = orderListDTO.getCurrentPage();
        Long pageSize = orderListDTO.getPageSize();
        Page<Order> orderPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = this.buildOrderListFilterCondition(orderListFilterDTO);
        Page<Order> orderPages = orderMapper.selectPage(orderPage, orderLambdaQueryWrapper);
        List<Order> records = orderPages.getRecords();
        List<OrderListVO> orderListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> orderIds = records.stream().map(Order::getId).toList();
            List<Long> userIds = records.stream().map(Order::getUserId).toList();
            List<Long> productIds = records.stream().map(Order::getProductId).toList();
            List<Long> receiptIds = records.stream().map(Order::getReceiptId).toList();
            // 构建用户Id和用户的映射Map
            Map<Long, User> userMap = this.getUserMap(userIds);
            // 构建商品Id和商品的映射Map
            Map<Long, ProductSuper> productSuperMap = this.getProductSuperMap(productIds);
            // 构建商品Id和图片的映射Map
            Map<Long, List<ProductImage>> productIdProductImageMap = new HashMap<>();
            List<ProductImage> productImages = productImageMapper.selectList(new LambdaQueryWrapper<ProductImage>()
                    .in(ProductImage::getProductId, productIds)
                    .eq(ProductImage::getMainFlag, Constants.NOT_MAIN_IMAGE_FLAG)
                    .eq(ProductImage::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            for (ProductImage productImage : productImages) {
                Long productId = productImage.getProductId();
                // 如果不存在，说明第一次插入，需要构建新数据
                // 否则更新
                if (!productIdProductImageMap.containsKey(productId)) {
                    productIdProductImageMap.put(productId, List.of(productImage));
                } else {
                    List<ProductImage> productImagesList = new ArrayList<>(productIdProductImageMap.get(productId));
                    productImagesList.add(productImage);
                    productIdProductImageMap.put(productId, productImagesList);
                }
            }
            // 构建地址Id和地址信息的映射Map
            Map<Long, Address> addressMap = this.getAddressMap(receiptIds);
            // 构建订单Id和退款信息的映射Map
            Map<Long, RefundInfo> orderIdRefundInfoMap = new HashMap<>();
            List<RefundInfo> refundInfos = refundInfoMapper.selectList(new LambdaQueryWrapper<RefundInfo>()
                    .in(RefundInfo::getOrderId, orderIds)
                    .eq(RefundInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            refundInfos.forEach(refundInfo -> orderIdRefundInfoMap.put(refundInfo.getOrderId(), refundInfo));

            records.forEach(record -> orderListVOS.add(this.buildOrderListVOFromOrder(record, userMap, productSuperMap,
                    productIdProductImageMap, addressMap, orderIdRefundInfoMap)));
        }

        return PageVO.<OrderListVO>builder()
                .currentPage(orderPages.getCurrent())
                .totalPages(orderPages.getPages())
                .totalCount(orderPages.getTotal())
                .totalRecords(orderListVOS)
                .build();
    }

    @Transactional
    @Override
    public Boolean change(AdminOrderChangeDTO adminOrderChangeDTO) {
        if (adminOrderChangeDTO == null) {
            throw new PetException("修改信息不全，无法进行订单修改");
        }

        Long userId = adminOrderChangeDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        Long orderId = adminOrderChangeDTO.getOrderId();
        Order existed = orderMapper.selectById(orderId);
        if (existed == null) {
            throw new PetException("指定订单不存在，修改失败");
        }
        if (!existed.getStatus().equals(Constants.ORDER_TO_PAY) &&
            !existed.getStatus().equals(Constants.ORDER_TO_DELIVER)) {
            throw new PetException("只能修改状态为待支付和待发货的订单");
        }
        String receiptName = adminOrderChangeDTO.getReceiptName();
        String phone = adminOrderChangeDTO.getPhone();
        String receiptAddress = adminOrderChangeDTO.getReceiptAddress();

        Order order = new Order();
        order.setId(orderId);
        if (StringUtils.hasText(receiptAddress)) {
            Address address = new Address();

            address.setAddressText(receiptAddress);
            String mapJson = baiduMapUtil.geographyEncoding(receiptAddress);
            List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
            address.setLatitude(latitudeAndLongitude.get(0));
            address.setLongitude(latitudeAndLongitude.get(1));
            boolean insertRet = addressMapper.insert(address) == 1;
            if (!insertRet) {
                throw new PetException("新地址插入失败");
            }
            order.setReceiptId(address.getId());
        }
        if (StringUtils.hasText(phone)) {
            order.setPhone(phone);
        }
        if (StringUtils.hasText(receiptName)) {
            order.setReceiptName(receiptName);
        }

        boolean updateRet = orderMapper.updateById(order) == 1;
        if (!updateRet) {
            throw new PetException("收货信息更新失败");
        }

        return true;
    }

    @Transactional
    @Override
    public Boolean handleRefund(HandleRefundDTO handleRefundDTO) {
        if (handleRefundDTO == null) {
            throw new PetException("订单信息错误，处理退款失败");
        }

        SecurityUtil.checkAdmin(handleRefundDTO.getUserId());
        Long orderId = handleRefundDTO.getOrderId();
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getRefundFlag().equals(Constants.ORDER_TO_REFUND)) {
            throw new PetException("订单不存在或者订单状态并不是退款");
        }

        Integer opFlag = handleRefundDTO.getOpFlag();
        Order refund = new Order();
        refund.setId(orderId);
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiverId(order.getUserId());
        commonMessage.setTitle(Constants.ORDER_SYSTEM_MESSAGE_TITLE);
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        if (opFlag.equals(Constants.REFUSE_REFUND)) {
            // 调用websocket发送退款失败通知，恢复订单退款状态
            String content = "您的订单" + "（编号为：" + orderId + "）" + "退款失败，具体原因可以询问客服。";
            commonMessage.setContent(content);
            refund.setRefundFlag(Constants.ORDER_NOT_REFUND);
        } else if (opFlag.equals(Constants.ALLOW_REFUND)) {
            // 修改订单标记
            refund.setStatus(Constants.ORDER_CANCELLED);
            // 移除退款标记
            refund.setRefundFlag(Constants.ORDER_NOT_REFUND);
            // 如果当前订单是已发货，则还需要删除物流信息表
            if (order.getStatus().equals(Constants.ORDER_DELIVERED) ||
                order.getStatus().equals(Constants.ORDER_TO_SIGN)) {
                boolean updateRet = orderLogisticsMapper.update(new LambdaUpdateWrapper<OrderLogistics>()
                        .eq(OrderLogistics::getOrderId, orderId)
                        .set(OrderLogistics::getDeleteFlag, Constants.DELETED_FLAG)) == 1;
                if (!updateRet) {
                    throw new PetException("物流信息更新失败");
                }
            }
            String content = "您的订单" + "（编号为：" + orderId + "）" + "已成功退款。";
            commonMessage.setContent(content);
        }
        boolean updateRet = orderMapper.updateById(refund) == 1;
        if (!updateRet) {
            throw new PetException("退款操作失败");
        }

        // 删除退款信息
        Long refundId = handleRefundDTO.getRefundId();
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setId(refundId);
        refundInfo.setDeleteFlag(Constants.DELETED_FLAG);
        boolean refundUpdateRet = refundInfoMapper.updateById(refundInfo) == 1;
        if (!refundUpdateRet) {
            throw new PetException("退款信息删除失败");
        }
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.ORDER_SYSTEM_MESSAGE);

        return true;
    }

    @Override
    public RefundInfoVO getRefund(Long orderId, Long userId) {
        if (userId == null || orderId == null) {
            throw new PetException("信息错误，获取退款信息失败");
        }

        SecurityUtil.checkAdmin(userId);
        RefundInfo refundInfo = refundInfoMapper.selectOne(new LambdaQueryWrapper<RefundInfo>()
                .eq(RefundInfo::getOrderId, orderId)
                .eq(RefundInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        return RefundInfoVO.builder()
                .orderId(refundInfo.getOrderId()).refundId(refundInfo.getId())
                .userId(refundInfo.getUserId()).message(refundInfo.getMessage())
                .createTime(refundInfo.getCreateTime())
                .build();
    }

    @Override
    public List<ExcelOrderBO> getOrdersForExcel(List<Long> orderIds) {
        List<Order> orders = new ArrayList<>();
        if (orderIds != null && !orderIds.isEmpty()) {
            orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().in(Order::getId, orderIds));
        } else {
            orders = orderMapper.selectList(null);
        }

        List<ExcelOrderBO> excelOrderBOS = new ArrayList<>();
        // 获取用户Map
        List<Long> userIds = orders.stream().map(Order::getUserId).toList();
        Map<Long, User> userMap = this.getUserMap(userIds);
        // 获取商品Map
        List<Long> productIds = orders.stream().map(Order::getProductId).toList();
        Map<Long, ProductSuper> productSuperMap = this.getProductSuperMap(productIds);
        // 获取地址Map
        List<Long> receiptIds = orders.stream().map(Order::getReceiptId).toList();
        Map<Long, Address> addressMap = this.getAddressMap(receiptIds);
        orders.forEach(order -> excelOrderBOS.add(this.buildExcelBOFromOrder(order, userMap, productSuperMap, addressMap)));
        return excelOrderBOS;
    }

    @Transactional
    @Override
    public Boolean deliver(Long orderId, Long userId) {
        if (userId == null || orderId == null) {
            throw new PetException("信息错误，发货失败");
        }

        SecurityUtil.checkAdmin(userId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new PetException("订单不存在，发货失败");
        }

        // 如果订单不处于待发货状态或者处于退款状态就不予发货
        if (!order.getStatus().equals(Constants.ORDER_TO_DELIVER) ||
            order.getRefundFlag().equals(Constants.ORDER_TO_REFUND)) {
            throw new PetException("订单状态错误或者订单处于退款中，无法发货");
        }

        // 修改为发货状态并更新物流表
        boolean orderUpdateRet = orderMapper.update(new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, Constants.ORDER_DELIVERED)) == 1;

        if (!orderUpdateRet) {
            throw new PetException("订单更新失败");
        }

        ProductSuper productSuper = productSuperMapper.selectById(order.getProductId());
        Integer type = productSuper.getType();
        Long shipId = productSuper.getShipId();
        Long receiptId = order.getReceiptId();

        Address shipAddress = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, shipId)
                .eq(Address::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Address receiptAddress = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, receiptId)
                .eq(Address::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        // 如果是宠物，则为空运
        // 否则陆运
        OrderLogistics orderLogistics = new OrderLogistics();
        orderLogistics.setOrderId(orderId);
        orderLogistics.setOriginLat(shipAddress.getLatitude());
        orderLogistics.setOriginLng(shipAddress.getLongitude());
        orderLogistics.setDestLat(receiptAddress.getLatitude());
        orderLogistics.setDestLng(receiptAddress.getLongitude());
        if (type.equals(Constants.PET)) {
            orderLogistics.setTransportType(Constants.AIR_TRANSPORT);
        } else if (type.equals(Constants.SUPPLY)) {
            orderLogistics.setTransportType(Constants.LAND_TRANSPORT);
        }

        boolean orderLogisticsInsertRet = orderLogisticsMapper.insert(orderLogistics) == 1;
        if (!orderLogisticsInsertRet) {
            throw new PetException("物流地址新增失败");
        }

        // 发送发货通知
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiverId(order.getUserId());
        commonMessage.setTitle(Constants.ORDER_SYSTEM_MESSAGE_TITLE);
        String content = "您的订单" + "（编号为：" + orderId + "）" + "已为您发货，您可以点击具体的订单查看物流状态。";
        commonMessage.setContent(content);
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.ORDER_SYSTEM_MESSAGE);

        return true;
    }

    private Map<Long, User> getUserMap(List<Long> userIds) {
        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds));
        users.forEach(user -> userMap.put(user.getId(), user));
        return userMap;
    }

    private Map<Long, Address> getAddressMap(List<Long> receiptIds) {
        Map<Long, Address> addressMap = new HashMap<>();
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .in(Address::getId, receiptIds)
                .eq(Address::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        addresses.forEach(address -> addressMap.put(address.getId(), address));
        return addressMap;
    }

    private Map<Long, ProductSuper> getProductSuperMap(List<Long> productIds) {
        Map<Long, ProductSuper> productSuperMap = new HashMap<>();
        List<ProductSuper> productSupers =
                productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>().in(ProductSuper::getId, productIds));
        productSupers.forEach(productSuper -> productSuperMap.put(productSuper.getId(), productSuper));
        return productSuperMap;
    }

    private ExcelOrderBO buildExcelBOFromOrder(Order order, Map<Long, User> userMap,
                                              Map<Long, ProductSuper> productSuperMap,
                                              Map<Long, Address> addressMap) {
        if (order == null) {
            return null;
        }

        Long receiptId = order.getReceiptId();
        Long productId = order.getProductId();
        Long userId = order.getUserId();

        String status = "";
        Integer refundFlag = order.getRefundFlag();
        if (refundFlag.equals(Constants.ORDER_NOT_REFUND)) {
            Integer orderStatus = order.getStatus();
            if (orderStatus.equals(Constants.ORDER_TO_PAY)) status = "待支付";
            if (orderStatus.equals(Constants.ORDER_TO_DELIVER)) status = "待发货";
            if (orderStatus.equals(Constants.ORDER_DELIVERED)) status = "已发货";
            if (orderStatus.equals(Constants.ORDER_TO_SIGN)) status = "待签收";
            if (orderStatus.equals(Constants.ORDER_SIGNED)) status = "已签收";
            if (orderStatus.equals(Constants.ORDER_CANCELLED)) status = "已取消";
        } else if (refundFlag.equals(Constants.ORDER_TO_REFUND)) {
            status = "退款中";
        }

        return ExcelOrderBO.builder()
                .orderId(order.getId()).productName(productSuperMap.get(productId).getName())
                .productType(productSuperMap.get(productId).getType().equals(Constants.PET) ? "宠物" : "宠物用品")
                .username(userMap.get(userId).getUsername()).receiptName(order.getReceiptName())
                .phone(order.getPhone()).receiptAddress(addressMap.get(receiptId).getAddressText())
                .totalCount(order.getTotalCount()).totalPrice(order.getTotalPrice())
                .createTime(order.getCreateTime()).status(status)
                .build();
    }

    private LambdaQueryWrapper<Order> buildOrderListFilterCondition(OrderListFilterDTO orderListFilterDTO) {
        if (orderListFilterDTO == null) {
            return null;
        }

        Long orderId = orderListFilterDTO.getOrderId();
        String productName = orderListFilterDTO.getProductName();
        String username = orderListFilterDTO.getUsername();
        Integer productType = orderListFilterDTO.getProductType();
        LocalDateTime startTime = orderListFilterDTO.getStartTime();
        LocalDateTime endTime = orderListFilterDTO.getEndTime();
        Integer status = orderListFilterDTO.getStatus();
        Integer refundFlag = orderListFilterDTO.getRefundFlag();
        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            orderLambdaQueryWrapper.eq(Order::getId, orderId);
        }

        if (StringUtils.hasText(productName)) {
            List<ProductSuper> products = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                    .like(ProductSuper::getName, productName));
            List<Long> productIds = new ArrayList<>();
            if (!products.isEmpty()) {
                productIds = products.stream().map(ProductSuper::getId).toList();
            }
            orderLambdaQueryWrapper.in(Order::getProductId, productIds);
        }
        if (StringUtils.hasText(username)) {
            orderLambdaQueryWrapper.like(Order::getReceiptName, username);
        }
        if (status != null) {
            orderLambdaQueryWrapper.eq(Order::getStatus, status)
                    .eq(Order::getRefundFlag, Constants.ORDER_NOT_REFUND);
        }
        if (refundFlag != null) {
            orderLambdaQueryWrapper.eq(Order::getRefundFlag, Constants.ORDER_TO_REFUND);
        }
        if (productType != null) {
            List<ProductSuper> products = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                    .eq(ProductSuper::getType, productType));
            List<Long> productIds = new ArrayList<>();
            if (!products.isEmpty()) {
                productIds = products.stream().map(ProductSuper::getId).toList();
            }
            orderLambdaQueryWrapper.in(Order::getProductId, productIds);
        }
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new PetException("结束时间不得早于开始时间");
            }
            orderLambdaQueryWrapper.between(Order::getCreateTime, startTime, endTime);
        }

        return orderLambdaQueryWrapper;
    }

    private OrderListVO buildOrderListVOFromOrder(Order order, Map<Long, User> userMap,
                                                 Map<Long, ProductSuper> productSuperMap,
                                                 Map<Long, List<ProductImage>> productIdProductImageMap,
                                                 Map<Long, Address> addressMap,
                                                 Map<Long, RefundInfo> orderIdRefundInfoMap) {
        if (order == null) {
            return null;
        }

        Long orderId = order.getId();
        Long userId = order.getUserId();
        Long productId = order.getProductId();
        // 用户可能曾经买过东西，但是之后注销了，所以此处不能只筛选出正常状态的用户

        // 为了保证商品的正常显示，下架和删除的商品也确保显示
        List<ProductImage> productImages = productIdProductImageMap.get(productId);
        String imageUrl = productImages.get(0).getImageUrl();

        Address address = addressMap.get(order.getReceiptId());

        Long refundId = 0L;
        if (!orderIdRefundInfoMap.isEmpty()) {
            RefundInfo refundInfo = orderIdRefundInfoMap.get(orderId);
            if (refundInfo != null) {
                refundId = refundInfo.getId();
            }
        }


        return OrderListVO.builder()
                .orderId(orderId).userId(userId).refundId(refundId)
                .username(userMap.get(userId).getUsername()).productId(productId)
                .productType(productSuperMap.get(productId).getType())
                .productName(productSuperMap.get(productId).getName()).imgUrl(imageUrl)
                .totalPrice(order.getTotalPrice()).totalCount(order.getTotalCount())
                .phone(order.getPhone()).receiptName(order.getReceiptName()).receiptAddress(address.getAddressText())
                .createTime(order.getCreateTime()).status(order.getStatus()).refundFlag(order.getRefundFlag())
                .build();
    }
}
