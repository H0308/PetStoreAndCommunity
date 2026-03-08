package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 19:57
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseProductAddDTO {
    @NotNull
    private Long userId;
    @NotNull
    @Length(max = 50, message = "产品名称最长不超过50个字符")
    private String productName;
    @NotNull
    @Length(max = 2048, message = "商品描述最长不超过2048个字符")
    private String description;
    @NotNull
    @Length(max = 255, message = "地址信息最长不超过255个字符")
    private String shipAddress;
    @NotNull
    private Long mainCategoryId;
    @NotNull
    private Long subCategoryId;
    @NotNull
    private BigDecimal price;
    private Long stock;
}
