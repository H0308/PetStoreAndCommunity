package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 猜你喜欢推荐商品 VO
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendProductVO {
    private Long productId;
    private String name;
    private Integer productType;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}
