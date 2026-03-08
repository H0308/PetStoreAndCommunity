package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 11:22
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileChangeDTO {
    @NotNull
    private Long userId;
    private Integer gender; // 1-男，2-女，3-保密
    private String username;
    private String profile;
}
