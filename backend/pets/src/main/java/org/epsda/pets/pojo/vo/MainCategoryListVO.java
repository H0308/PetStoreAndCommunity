package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 15:18
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainCategoryListVO {
    private Long categoryId;
    private Integer type;
    private String categoryName;
    private Long subCategoryCount;
    private Long productCount;
}
