package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/14
 * Time: 12:15
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnderMainCategoryWithFilterDTO {
    private ProductUnderMainCategoryDTO productUnderMainCategoryDTO;
    private CategoryFilterDTO categoryFilterDTO;
}
