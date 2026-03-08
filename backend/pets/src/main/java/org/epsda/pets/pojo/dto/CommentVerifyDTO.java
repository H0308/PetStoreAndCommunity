package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/15
 * Time: 12:03
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVerifyDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long commentId;
    @NotNull
    private Integer opFlag; // 0-不予通过，1-审核通过
}
