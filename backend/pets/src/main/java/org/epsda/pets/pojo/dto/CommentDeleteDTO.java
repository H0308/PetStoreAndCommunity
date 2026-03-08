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
 * Time: 11:10
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDeleteDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long userIdComment;
    @NotNull
    private Long commentId;
    @NotNull
    private Integer commentType;
}
