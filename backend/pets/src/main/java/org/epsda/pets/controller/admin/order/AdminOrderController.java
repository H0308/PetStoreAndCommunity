package org.epsda.pets.controller.admin.order;

import com.alibaba.excel.EasyExcel;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.bo.ExcelOrderBO;
import org.epsda.pets.pojo.dto.AdminOrderChangeDTO;
import org.epsda.pets.pojo.dto.HandleRefundDTO;
import org.epsda.pets.pojo.dto.OrderListWithFilterDTO;
import org.epsda.pets.pojo.vo.OrderListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.RefundInfoVO;
import org.epsda.pets.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/03
 * Time: 21:25
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    // 分页显示和根据条件筛选与搜索订单
    @Operation(summary = "获取订单")
    @PostMapping("/list")
    public ResultWrapper<PageVO<OrderListVO>> list(@Validated @RequestBody OrderListWithFilterDTO orderListWithFilterDTO) {
        return ResultWrapper.normal(adminOrderService.list(orderListWithFilterDTO.getOrderListDTO(),
                orderListWithFilterDTO.getOrderListFilterDTO()));
    }

    // 修改用户收货信息
    @Operation(summary = "修改用户收货信息")
    @PostMapping("/change")
    public ResultWrapper<Boolean> change(@Validated @RequestBody AdminOrderChangeDTO adminOrderChangeDTO) {
        return ResultWrapper.normal(adminOrderService.change(adminOrderChangeDTO));
    }

    // 查看退款信息
    @Operation(summary = "查看退款信息")
    @GetMapping("/getRefund")
    public ResultWrapper<RefundInfoVO> getRefund(@NotNull @RequestParam("orderId") Long orderId,
                                                 @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminOrderService.getRefund(orderId, userId));
    }

    // 处理退款
    @Operation(summary = "处理退款")
    @PostMapping("/handleRefund")
    public ResultWrapper<Boolean> handleRefund(@Validated @RequestBody HandleRefundDTO handleRefundDTO) {
        return ResultWrapper.normal(adminOrderService.handleRefund(handleRefundDTO));
    }

    // 发货
    @Operation(summary = "发货")
    @PostMapping("/deliver")
    public ResultWrapper<Boolean> deliver(@NotNull @RequestParam("orderId") Long orderId,
                                          @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminOrderService.deliver(orderId, userId));
    }

    // 导出指定的订单数据
    @Operation(summary = "导出指定的订单数据")
    @GetMapping("/excel")
    @SneakyThrows
    public void downloadExcel(@RequestParam(required = false) List<Long> orderIds, HttpServletResponse response){
        List<ExcelOrderBO> excelOrderBOS = adminOrderService.getOrdersForExcel(orderIds);
        // 设置响应头
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding("utf-8");
        String filename = "pets-" + new Date() + "-订单明细表";
        // 防止中文乱码
        String fileName = URLEncoder.encode(filename, StandardCharsets.UTF_8).
                replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        // 使用 EasyExcel 写入到响应输出流
        EasyExcel.write(response.getOutputStream(), ExcelOrderBO.class)
                .sheet("订单明细表")
                .doWrite(excelOrderBOS);
    }
}
