package org.epsda.pets.pojo.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/04
 * Time: 20:25
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@ColumnWidth(20)
public class ExcelOrderBO {
    @ExcelProperty("账单ID")
    private Long orderId;
    @ExcelProperty("商品名称")
    private String productName;
    @ExcelProperty("商品类型")
    private String productType;
    @ExcelProperty("用户名")
    private String username;
    @ExcelProperty("用户收货名称")
    private String receiptName;
    @ExcelProperty("用户收货电话")
    private String phone;
    @ExcelProperty("用户收货地址")
    private String receiptAddress;
    @ExcelProperty("购买数量")
    private Long totalCount;
    @ExcelProperty("支付金额")
    private BigDecimal totalPrice;
    @ExcelProperty("下单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ExcelProperty("订单状态")
    private String status;
}
