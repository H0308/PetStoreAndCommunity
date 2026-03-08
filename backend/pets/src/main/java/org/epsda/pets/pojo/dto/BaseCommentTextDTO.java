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
 * Date: 2025/12/19
 * Time: 11:01
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseCommentTextDTO {
    @NotNull
    private Long objectId;
    @NotNull
    private Long userId;
    private Long parentId; // 如果是顶级评论则不需要传递该字段
    @NotNull
    @Length(max = 2048, message = "评论内容最多不超过2048个字符")
    private String content;
}
