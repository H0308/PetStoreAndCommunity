package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 18:48
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListVO {
    private Long productId;
    private String productName;
    private String identifier;
    private Integer type; // 1-宠物，2-宠物用品
    private Long mainCategoryId;
    private String mainCategoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private String productImageUrl;
    private String shipAddress;
    private Long stock;
    private Integer status;
    private BigDecimal price;
}
