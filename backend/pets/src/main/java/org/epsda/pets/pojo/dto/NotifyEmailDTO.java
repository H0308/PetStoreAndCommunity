package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 10:39
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyEmailDTO {
    @NotNull
    private Long userId; // 管理员ID
    @NotNull
    private String email; // 目标用户
    @NotNull
    private String subject; // 邮件主题
    @NotNull
    private String content; // 邮件内容
}
