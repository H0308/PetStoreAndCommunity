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
 * Date: 2026/01/10
 * Time: 18:18
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveWordAddDTO {
    @NotNull
    private Long userId;
    @NotNull
    @Length(max = 255, message = "敏感词长度最长不超过255个字符")
    private String word;
    @NotNull
    private Long categoryId;
}
