package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 15:12
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailListVO {
    private Long userId;
    private String avatarUrl;
    private String username;
    private String email;
    private String phone;
    private Integer gender; // 性别，1-男，2-女，3-保密
    private String receiptName;
    private String receiptAddress;
    private Integer realNameAuthFlag; // 是否实名认证，0-否，1-是
    private Integer status; // 销户状态，0-未销户，1-已销户
    private Integer banFlag; // 禁言状态，0-未禁言，1-已禁言
    private Long roleId; // 角色状态，0-管理员，1-普通用户
}
