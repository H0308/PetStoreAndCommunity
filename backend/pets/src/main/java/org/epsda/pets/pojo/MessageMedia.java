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
 * Date: 2025/12/26
 * Time: 20:00
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_message_media")
public class MessageMedia {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long messageId;
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
