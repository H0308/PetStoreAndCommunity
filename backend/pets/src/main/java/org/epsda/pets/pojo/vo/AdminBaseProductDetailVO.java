package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:02
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminBaseProductDetailVO {
    private String identifier; // 显示但不可修改
    private String productName;
    private String description;
    private Integer type;
    private String shipAddress;
    private Long mainCategoryId;
    private Long subCategoryId;
    private BigDecimal price;
    private Long stock;
    private Integer status; // 显示但不可修改
    private Map<Long, String> productImageUrls;
}
