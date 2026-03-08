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
 * Time: 15:53
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderChangeDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long orderId;
    private String receiptName;
    private String receiptAddress; // 修改了地址信息后需要传递实际地址名称
    private String phone;
}
