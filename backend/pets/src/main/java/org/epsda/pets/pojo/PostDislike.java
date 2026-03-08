package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/21
 * Time: 12:18
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_post_dislike")
public class PostDislike {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long postId;
    private Integer cancelFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
