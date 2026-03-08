package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 20:21
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterDTO {
    private Long productId;
    private String keyword;
    private Long mainCategoryId;
    private Long subCategoryId;
    private Integer type;
    private Integer status;
}
