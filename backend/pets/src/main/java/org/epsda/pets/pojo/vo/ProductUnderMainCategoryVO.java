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
 * Date: 2025/12/12
 * Time: 17:43
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnderMainCategoryVO {
    private Long productId;
    private String name;
    private Integer productType;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}
