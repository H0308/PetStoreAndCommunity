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
 * Time: 10:22
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_post_media")
public class PostMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long postId;
    @NotNull
    @Length(max = 1024, message = "媒体地址最长不超过1024个字符")
    private String mediaUrl;
    @NotNull
    private Integer mediaType;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}