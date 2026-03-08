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
@TableName("tb_product_comment_sub")
public class ProductCommentSub {
    @TableId(type = IdType.AUTO)
    private Long subId;
    @NotNull
    private Long commentId;
    private Long stars;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime subCreateTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime subUpdateTime;
}