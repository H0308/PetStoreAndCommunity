package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/04
 * Time: 15:07
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListFilterDTO {
    private Long orderId;
    private String productName;
    private String username;
    private Integer productType;
    private Integer status;
    private Integer refundFlag;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
