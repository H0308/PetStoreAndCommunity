package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 20:06
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseCommentVO {
    private Long commentId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long parentId; // 为空时代表是顶级评论
    private String content;
    private Integer status;
    private Integer deleteFlag;
    private List<CommentMediaVO> mediaVOS;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
