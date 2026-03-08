package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 22:32
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderChangeUserInfoDTO {
    @NotNull
    private Long userId; // 不可修改
    private String receiptName;
    private Long receiptId; // 如果没有修改地址信息，则传递，否则为空，传递下面的地址名称参数
    private String receiptAddress; // 修改了地址信息后需要传递实际地址名称
    private String phone;
}
