package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:38
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SensitiveWordListDTO extends PageDTO{
    @NotNull
    private Long userId;
}
