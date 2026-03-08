package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:37
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseProductChangeDTO {
    private Integer type;
    @Length(max = 50, message = "产品名称最长不超过50个字符")
    private String productName;
    @Length(max = 2048, message = "商品描述最长不超过2048个字符")
    private String description;
    @Length(max = 255, message = "地址信息最长不超过255个字符")
    private String shipAddress;
    private Long mainCategoryId;
    private Long subCategoryId;
    private BigDecimal price;
    private Long stock;
    private Map<Long, Integer> productImageUrls; // value为删除标记，0-未删除，1表示删除
}
