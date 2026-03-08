package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/01
 * Time: 18:03
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainCategoryChangeDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long categoryId;
    private Integer type;
    private String categoryName;
}
