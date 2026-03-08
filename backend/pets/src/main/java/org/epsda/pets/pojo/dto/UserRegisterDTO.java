package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/03
 * Time: 17:56
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    @NotNull
    @Length(max = 30, message = "用户名最长不超过30个字符")
    private String username;
    @NotNull
    @Length(max = 30, message = "邮箱最长不超过30个字符")
    private String email;
    @NotNull
    @Length(max = 30, message = "密码最长不超过30个字符")
    private String password;
    @NotNull
    @Length(max = 30, message = "密码最长不超过30个字符")
    private String confirmPassword;
}
