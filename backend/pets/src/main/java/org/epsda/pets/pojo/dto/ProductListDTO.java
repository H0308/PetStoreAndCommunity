package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 18:57
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductListDTO extends PageDTO {
    @NotNull
    private Long userId;
}
