package org.epsda.pets.controller.user.order;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 15:13
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/order")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 获取当前用户的所有订单信息
    @Operation(summary = "获取当前用户的所有订单信息")
    @PostMapping("/list")
    public ResultWrapper<PageVO<OrderDetailVO>> list(@Validated @RequestBody OrdersDetailWithFilterDTO ordersDetailWithFilterDTO) {
        return ResultWrapper.normal(orderService.list(ordersDetailWithFilterDTO.getOrdersDetailDTO(),
                ordersDetailWithFilterDTO.getOrderFilterDTO()));
    }

    // 根据订单ID获取指定的订单信息
    @Operation(summary = "根据订单ID获取指定的订单信息")
    @GetMapping("/getOne")
    public ResultWrapper<OrderDetailVO> getOne(@NotNull Long orderId, @NotNull Long userId) {
        return ResultWrapper.normal(orderService.getOne(orderId, userId));
    }

    // 预创建订单
    @Operation(summary = "预创建商品订单")
    @PostMapping("/preCreate")
    public ResultWrapper<PreOrderDetailVO> preCreate(@Validated @RequestBody OrderPreCreateDTO orderPreCreateDTO) {
        return ResultWrapper.normal(orderService.preCreate(orderPreCreateDTO));
    }

    // 修改预创建订单
    @Operation(summary = "修改预创建订单的用户个人信息")
    @PostMapping("/changeUserInfo")
    public ResultWrapper<ChangeOrderUserInfoVO> changeUserInfo(@Validated @RequestBody OrderChangeUserInfoDTO orderChangeUserInfoDTO) {
        return ResultWrapper.normal(orderService.changeUserInfo(orderChangeUserInfoDTO));
    }

    @Operation(summary = "修改预创建订单的商品个数")
    @PostMapping("/changeProductCount")
    public ResultWrapper<ChangeOrderProductCountVO> changeProductCount(@Validated @RequestBody OrderChangeProductCountDTO orderChangeProductCountDTO) {
        return ResultWrapper.normal(orderService.changeProductCount(orderChangeProductCountDTO));
    }

    // 最终确定订单
    @Operation(summary = "创建商品订单")
    @PostMapping("/create")
    public ResultWrapper<OrderDetailVO> create(@Validated @RequestBody OrderConfirmDetailDTO orderConfirmDetailDTO) {
        return ResultWrapper.normal(orderService.create(orderConfirmDetailDTO));
    }

    // 批量确认订单
    @Operation(summary = "批量创建商品订单")
    @PostMapping("/batchCreate")
    public ResultWrapper<List<OrderDetailVO>> batchCreate(@RequestBody List<OrderConfirmDetailDTO> orderConfirmDetailDTOs) {
        return ResultWrapper.normal(orderService.batchCreate(orderConfirmDetailDTOs));
    }

    // 支付订单
    @Operation(summary = "支付指定订单")
    @PostMapping("/pay")
    public ResultWrapper<PayOrderVO> pay(@Validated @RequestBody PayOrderDTO payOrderDTO) {
        return ResultWrapper.normal(orderService.pay(payOrderDTO));
    }

    // 批量支付订单
    @Operation(summary = "批量支付订单")
    @PostMapping("/batchPay")
    public ResultWrapper<List<PayOrderVO>> batchPay(@RequestBody List<PayOrderDTO> payOrderDTOs) {
        return ResultWrapper.normal(orderService.batchPay(payOrderDTOs));
    }

    // 取消订单
    @Operation(summary = "取消指定订单")
    @PostMapping("/cancel")
    public ResultWrapper<Boolean> cancel(@Validated @RequestBody CancelOrderDTO cancelOrderDTO) {
        return ResultWrapper.normal(orderService.cancel(cancelOrderDTO));
    }

    // 批量取消订单
    @Operation(summary = "批量取消订单")
    @PostMapping("/batchCancel")
    public ResultWrapper<List<Boolean>> batchCancel(@RequestBody List<CancelOrderDTO> cancelOrderDTOs) {
        return ResultWrapper.normal(orderService.batchCancel(cancelOrderDTOs));
    }

    // 将用户保存的地址和电话信息同步到用户表中
    @Operation(summary = "将用户保存的地址和电话信息同步到用户表中")
    @PostMapping("/save")
    public ResultWrapper<Boolean> save(@Validated @RequestBody SaveOrderUserInfoDTO saveOrderUserInfoDTO) {
        return ResultWrapper.normal(orderService.save(saveOrderUserInfoDTO));
    }

    // 签收
    @Operation(summary = "签收")
    @PostMapping("/receive")
    public ResultWrapper<Boolean> receive(@Validated @RequestBody ReceiveOrderDTO receiveOrderDTO) {
        return ResultWrapper.normal(orderService.receive(receiveOrderDTO));
    }

    // 退款
    @Operation(summary = "退款")
    @PostMapping("/refund")
    public ResultWrapper<Boolean> refund(@Validated @RequestBody RefundDTO refundDTO) {
        return ResultWrapper.normal(orderService.refund(refundDTO));
    }

    // 取消退款
    @Operation(summary = "取消退款")
    @PostMapping("/cancelRefund")
    public ResultWrapper<Boolean> cancelRefund(@Validated @RequestBody CancelRefundDTO cancelRefundDTO) {
        return ResultWrapper.normal(orderService.cancelRefund(cancelRefundDTO));
    }

    // 获取物流信息
    @Operation(summary = "获取物流信息")
    @PostMapping("/logistics")
    public ResultWrapper<TransportVO> logistics(@Validated @RequestBody TransportDTO transportDTO) {
        return ResultWrapper.normal(orderService.logistics(transportDTO));
    }

    // 存储当前路径点并判断是否到达收货地点
    @Operation(summary = "存储当前路径点并判断是否到达收货地点")
    @PostMapping("/checkDelivered")
    public ResultWrapper<TransportArrivedVO> checkDelivered(@Validated @RequestBody CurrentTransportDTO currentTransportDTO) {
        return ResultWrapper.normal(orderService.checkDelivered(currentTransportDTO));
    }
}
