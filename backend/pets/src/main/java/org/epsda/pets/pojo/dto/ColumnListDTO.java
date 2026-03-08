package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 17:32
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ColumnListDTO extends PageDTO {
    @NotNull
    private Long userId;
}
