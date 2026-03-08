package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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
@TableName("tb_topic_post")
public class TopicPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long postId;
    @NotNull
    private Long topicId;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}