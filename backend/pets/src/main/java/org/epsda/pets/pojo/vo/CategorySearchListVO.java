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
 * Time: 16:30
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySearchListVO {
    private PageVO<MainCategoryListVO> mainCategoryListVOPageVO;
    private PageVO<SubCategoryListVO> subCategoryListVOPageVO;
}
