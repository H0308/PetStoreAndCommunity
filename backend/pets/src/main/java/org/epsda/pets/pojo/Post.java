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
@TableName("tb_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private Long columnId;
    @NotNull
    @Length(max = 50, message = "帖子标题最长不超过50个字符")
    private String title;
    @Length(max = 2048, message = "帖子内容最长不超过2048个字符")
    private String content;
    private Integer status;
    private Long likeCount;
    private Long rejectCount;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}