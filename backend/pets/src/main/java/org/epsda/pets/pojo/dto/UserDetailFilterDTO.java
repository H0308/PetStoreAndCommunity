package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 16:08
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserDetailFilterDTO extends PageDTO{
    @NotNull
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private Long roleId;
    private Integer status;
    private Integer banFlag;
    private Integer realNameAuthFlag;
}
