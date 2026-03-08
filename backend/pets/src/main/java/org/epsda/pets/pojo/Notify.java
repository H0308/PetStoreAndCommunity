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
 * Date: 2026/02/14
 * Time: 19:08
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_notify")
public class Notify {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long receiveUserId;
    private String title;
    private String content;
    private Integer type;
    private Integer status;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
