package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/11
 * Time: 21:02
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveCategoryChangeDTO {
    @NotNull
    private Long categoryId; // 不可变
    @NotNull
    private Long userId; // 不可变
    private String name; // 发生修改时传递，否则传递null
}
