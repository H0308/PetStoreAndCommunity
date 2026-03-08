package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/04
 * Time: 17:21
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandleRefundDTO {
    @NotNull
    private Long refundId;
    @NotNull
    private Long orderId;
    @NotNull
    private Long userIdNotify;
    @NotNull
    private Long userId;
    @NotNull
    private Integer opFlag; // 0-不予退款，1-予以退款
}
