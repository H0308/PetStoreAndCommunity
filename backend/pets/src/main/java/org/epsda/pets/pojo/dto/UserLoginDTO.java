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
 * Date: 2025/12/13
 * Time: 17:08
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    @NotNull
    @Length(max = 30, message = "邮箱长度最长不超过30个字符")
    public String email;
    @NotNull
    @Length(max = 30, message = "密码长度最长不超过30个字符")
    public String password;
}
