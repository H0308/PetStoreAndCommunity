package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/21
 * Time: 13:29
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDislikeDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long postId;
    @NotNull
    private Integer opFlag; // 0-点踩，1-取消点踩
}
