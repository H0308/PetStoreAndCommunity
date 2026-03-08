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
@TableName("tb_pet_supply_sub")
public class PetSupplySub {
    @TableId(type = IdType.AUTO)
    private Long subId;
    @NotNull
    private Long productId;
    @Length(max = 50, message = "宠物用品品牌最长不超过50个字符")
    private String brand;
    @Length(max = 255, message = "适用年龄段最长不超过255个字符")
    private String fitAge;
    @Length(max = 255, message = "适用品种最长不超过255个字符")
    private String fitVariety;
    private LocalDateTime manufactureDate;
    private LocalDateTime guaranteeDate;
    @Length(max = 255, message = "生产公司最长不超过255个字符")
    private String company;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime subCreateTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime subUpdateTime;
}