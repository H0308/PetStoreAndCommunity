package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/01
 * Time: 12:29
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long mainCategoryId;
    @NotNull
    @Length(max = 50, message = "分类名称最长不超过50个字符")
    private String categoryName;
}
