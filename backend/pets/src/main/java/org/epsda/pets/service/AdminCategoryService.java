package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CategorySearchListVO;
import org.epsda.pets.pojo.vo.MainCategoryListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SubCategoryListVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 15:45
 *
 * @Author: 憨八嘎
 */
public interface AdminCategoryService {
    PageVO<MainCategoryListVO> mainCategory(MainCategoryListDTO mainCategoryListDTO);

    PageVO<SubCategoryListVO> subCategory(SubCategoryListDTO subCategoryListDTO);

    CategorySearchListVO search(CategorySearchListDTO categorySearchListDTO);

    Boolean addMainCategory(MainCategoryAddDTO mainCategoryAddDTO);

    Boolean addSubCategory(SubCategoryDTO subCategoryDTO);

    Boolean changeMainCategory(MainCategoryChangeDTO mainCategoryChangeDTO);

    Boolean changeSubCategory(SubCategoryChangeDTO subCategoryChangeDTO);

    Boolean deleteMainCategory(Long mainCategoryId, Long userId);

    Boolean deleteSubCategory(Long subCategoryId, Long userId);

    Boolean batchDeleteMainCategory(List<Long> mainCategoryIds, Long userId);

    Boolean batchDeleteSubCategory(List<Long> subCategoryIds, Long userId);
}
