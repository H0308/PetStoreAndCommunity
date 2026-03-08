package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 19:56
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailChangeDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long userIdChange;
    private Long roleId;
    private String phone;
    private String email;
    private String receiptName;
    private String receiptAddress;
}
