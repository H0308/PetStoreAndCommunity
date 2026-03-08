package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 10:46
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryFilterDTO {
    private Integer sellCount; // 0-销量升序，1-销量降序
    private Integer latest; // 0-不启用，1-创建时间降序
    private Integer price; // 0-价格升序，1-价格降序
    private BigDecimal miniPrice; // 最低价格
    private BigDecimal maxPrice; // 最高价格
}
