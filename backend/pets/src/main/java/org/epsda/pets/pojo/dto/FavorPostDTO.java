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
 * Time: 20:01
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavorPostDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long postId;
    @NotNull
    private Integer opFlag; // 0-收藏，1-取消收藏
}
