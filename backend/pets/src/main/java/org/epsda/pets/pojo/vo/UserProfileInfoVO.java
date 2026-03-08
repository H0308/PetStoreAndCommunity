package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 11:06
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileInfoVO {
    private String username;
    private Integer gender; // 1-男，2-女，3-保密
    private Long roleId; // 1-普通用户，0-管理员
    private Integer banFlag; // 0-未禁言，1-已禁言
    private Integer realNameAuthFlag; // 是否实名认证，0-否，1-是
    private String avatarUrl;
    private String profile;
}
