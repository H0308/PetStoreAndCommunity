package org.epsda.pets.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/18
 * Time: 11:08
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVO {
    private Long postId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long columnId;
    private String title;
    private String mediaUrl;
    private Long likeCount;
}
