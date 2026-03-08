package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/03
 * Time: 21:38
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListVO {
    private Long orderId;
    private Long refundId;
    private Long userId;
    private String username;
    private Long productId;
    private Integer productType;
    private String productName;
    private String imgUrl;
    private Long totalCount;
    private BigDecimal totalPrice;
    private String phone;
    private String receiptName;
    private String receiptAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private Integer status;
    private Integer refundFlag;
}
