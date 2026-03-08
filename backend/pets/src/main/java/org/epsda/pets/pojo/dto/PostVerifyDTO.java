package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 11:08
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVerifyDTO {
    @NotNull
    private Long postId;
    @NotNull
    private Long userId;
    @NotNull
    private Integer opFlag; // 0-不予通过，1-审核通过
}
