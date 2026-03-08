package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
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
@TableName("tb_product_super")
public class ProductSuper {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    @Length(max = 255, message = "商品编号最长不超过255个字符")
    private String identifier;
    @NotNull
    @Length(max = 50, message = "商品名称最长不超过50个字符")
    private String name;
    @NotNull
    @Length(max = 1024, message = "商品描述最长不超过1024个字符")
    private String description;
    @NotNull
    private Integer type;
    @NotNull
    private Long shipId;
    @NotNull
    private Long mainCategoryId;
    @NotNull
    private Long subCategoryId;
    @NotNull
    private BigDecimal price;
    private Long stock;
    private Integer status;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}