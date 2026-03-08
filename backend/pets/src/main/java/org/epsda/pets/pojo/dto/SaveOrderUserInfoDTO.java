package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/15
 * Time: 22:12
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveOrderUserInfoDTO {
    @NotNull
    private Long userId;
    private String phone;
    private String receiptName;
    private Long receiptId;
}
