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
@TableName("tb_product_category_sub")
public class ProductCategorySub {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long mainCategoryId;
    @NotNull
    @Length(max = 50, message = "二级分类名称最长不超过50个字符")
    private String name;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}