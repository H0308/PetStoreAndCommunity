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
@TableName("tb_message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long receiveUserId;
    @NotNull
    private Long sendUserId;
    @Length(max = 2000, message = "消息内容最多不超过2000个字符")
    private String message;
    private Integer type;
    private Integer status;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}