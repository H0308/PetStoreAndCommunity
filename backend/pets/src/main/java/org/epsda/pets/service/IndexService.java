package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.CategoryFilterDTO;
import org.epsda.pets.pojo.dto.ProductUnderMainCategoryDTO;
import org.epsda.pets.pojo.dto.ProductUnderSubCategoryDTO;
import org.epsda.pets.pojo.vo.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 17:00
 *
 * @Author: 憨八嘎
 */
public interface IndexService {
    List<CarouselVO> getCarousels();

    List<SuperCategoryVO> getSuperCategories();

    List<SubCategoryVO> getSubCategories(Long mainCategoryId);

    List<LatestProductsVO> getLatestProducts();

    PageVO<ProductUnderMainCategoryVO> getProductsUnderMainCategory(ProductUnderMainCategoryDTO productUnderMainCategoryDTO, CategoryFilterDTO categoryFilterDTO);

    PageVO<ProductUnderSubCategoryVO> getProductsUnderSubCategory(ProductUnderSubCategoryDTO productUnderSubCategoryDTO, CategoryFilterDTO categoryFilterDTO);

    List<RecommendProductVO> getRecommendations(Long userId, Integer defaultTopN);
}
