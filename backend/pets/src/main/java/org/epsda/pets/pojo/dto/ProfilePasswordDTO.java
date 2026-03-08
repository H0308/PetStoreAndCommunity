package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/23
 * Time: 12:24
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfilePasswordDTO {
    @NotNull
    private Long userId;
    @NotNull
    private String oldPassword;
    @NotNull
    private String newPassword;
    @NotNull
    private String confirmPassword;
}
