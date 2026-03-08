package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 20:07
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplyProductAddDTO extends BaseProductAddDTO{
    @Length(max = 50, message = "品牌名称最长不超过50个字符")
    private String brand;
    @Length(max = 255, message = "适用年龄最长不超过255个字符")
    private String fitAge;
    @Length(max = 255, message = "适用品种最长不超过255个字符")
    private String fitVariety;
    @NotNull
    private LocalDateTime manufactureDate;
    @NotNull
    private LocalDateTime guaranteeDate;
    @Length(max = 255, message = "公司名称最长不超过255个字符")
    private String company;
}
