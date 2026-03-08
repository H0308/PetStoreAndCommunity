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
 * Date: 2026/02/13
 * Time: 17:16
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_message_latest")
public class MessageLatest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sendUserId;
    private Long receiveUserId;
    private Long messageId;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
