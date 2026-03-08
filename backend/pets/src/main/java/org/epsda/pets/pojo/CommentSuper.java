package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:21
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_comment_super")
public class CommentSuper {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long objectId;
    @NotNull
    private Long userId;
    private Long parentId;
    @NotNull
    private Integer type;
    @NotNull
    @Length(max = 2048, message = "评论内容最多不超过2048个字符")
    private String content;
    private Integer status;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}