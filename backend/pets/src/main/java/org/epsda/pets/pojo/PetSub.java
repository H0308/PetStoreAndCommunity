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
@TableName("tb_pet_sub")
public class PetSub {
    @TableId(type = IdType.AUTO)
    private Long subId;
    @NotNull
    private Long productId;
    private Integer healthStatus;
    private String trainNote;
    private String raiseNote;
    private Integer vaccineFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime subCreateTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime subUpdateTime;
}