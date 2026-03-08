package org.epsda.pets.service;

import org.epsda.pets.pojo.bo.ExcelOrderBO;
import org.epsda.pets.pojo.dto.AdminOrderChangeDTO;
import org.epsda.pets.pojo.dto.HandleRefundDTO;
import org.epsda.pets.pojo.dto.OrderListDTO;
import org.epsda.pets.pojo.dto.OrderListFilterDTO;
import org.epsda.pets.pojo.vo.OrderListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.RefundInfoVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/03
 * Time: 21:35
 *
 * @Author: 憨八嘎
 */
public interface AdminOrderService {
    PageVO<OrderListVO> list(OrderListDTO orderListDTO, OrderListFilterDTO orderListFilterDTO);

    Boolean change(AdminOrderChangeDTO adminOrderChangeDTO);

    Boolean handleRefund(HandleRefundDTO handleRefundDTO);

    RefundInfoVO getRefund(Long orderId, Long userId);

    List<ExcelOrderBO> getOrdersForExcel(List<Long> orderIds);

    Boolean deliver(Long orderId, Long userId);
}
