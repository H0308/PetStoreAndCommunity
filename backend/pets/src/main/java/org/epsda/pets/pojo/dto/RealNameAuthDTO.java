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
 * Time: 11:55
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RealNameAuthDTO {
    @NotNull
    private Long userId;
    @NotNull
    private String realName;
    @NotNull
    private String idCard;
}
