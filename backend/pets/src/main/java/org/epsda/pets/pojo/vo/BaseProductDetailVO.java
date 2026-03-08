package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 11:22
 *
 * @Author: 憨八嘎
 */
@SuperBuilder // 确保子类可以设置父类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseProductDetailVO {
    private Long productId;
    private String identifier;
    private Integer productType; // 1-宠物，2-宠物用品
    private String deliverAddress;
    private String name;
    private String description;
    private String mainCategoryName;
    private String subCategoryName;
    private Integer status;
    private BigDecimal price;
    private Long stock;
    private List<String> productImages;
}
