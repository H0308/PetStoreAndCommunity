package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;

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
public interface OrderService {
    PageVO<OrderDetailVO> list(OrdersDetailDTO ordersDetailDTO, OrderFilterDTO orderFilterDTO);

    PreOrderDetailVO preCreate(OrderPreCreateDTO orderPreCreateDTO);

    OrderDetailVO create(OrderConfirmDetailDTO orderConfirmDetailDTO);

    ChangeOrderUserInfoVO changeUserInfo(OrderChangeUserInfoDTO orderChangeDTO);

    ChangeOrderProductCountVO changeProductCount(OrderChangeProductCountDTO orderChangeProductCountDTO);

    OrderDetailVO getOne(Long orderId, Long userId);

    PayOrderVO pay(PayOrderDTO payOrderDTO);

    Boolean cancel(CancelOrderDTO cancelOrderDTO);

    Boolean save(SaveOrderUserInfoDTO saveOrderUserInfoDTO);

    List<OrderDetailVO> batchCreate(List<OrderConfirmDetailDTO> orderConfirmDetailDTOs);

    List<PayOrderVO> batchPay(List<PayOrderDTO> payOrderDTOs);

    List<Boolean> batchCancel(List<CancelOrderDTO> cancelOrderDTOs);

    Boolean refund(RefundDTO refundDTO);

    Boolean receive(ReceiveOrderDTO receiveOrderDTO);

    Boolean cancelRefund(CancelRefundDTO cancelRefundDTO);

    TransportVO logistics(TransportDTO transportDTO);

    TransportArrivedVO checkDelivered(CurrentTransportDTO currentTransportDTO);
}
