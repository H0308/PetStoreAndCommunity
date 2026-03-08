package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/13
 * Time: 16:08
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentListVO {
    private Long commentId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Integer commentType;
    private Long stars; // 只有是商品评论且评论不处于删除状态才显示
    private Long objectId; // 被评论对象ID
    private String objectName; // 商品标题或者帖子标题
    private Long parentId; // 父级评论ID，顶级评论时为null
    private String parentContent; // 父级评论内容，顶级评论时为null
    private String content; // 评论内容
    private Long mediaCount;
    private List<String> mediaUrls; // 显示在评论详情中
    private Long replyCount;
    private Integer status;
    private Integer deleteFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
