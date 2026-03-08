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
 * Date: 2026/01/11
 * Time: 20:54
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveCategoryAddDTO {
    @NotNull
    private Long userId;
    @NotNull
    @Length(max = 50, message = "敏感词分类名称长度不超过50个字符")
    private String name;
}
