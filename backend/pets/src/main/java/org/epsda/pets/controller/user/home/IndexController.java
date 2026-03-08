package org.epsda.pets.controller.user.home;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 15:20
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    // 首页轮播图展示
    @Operation(summary = "首页轮播图展示")
    @GetMapping("/getCarousels")
    public ResultWrapper<List<CarouselVO>> getCarousels() {
        return ResultWrapper.normal(indexService.getCarousels());
    }

    // 轮播图上方悬浮一级分类
    @Operation(summary = "轮播图上方悬浮一级分类")
    @GetMapping("/getSuperCategories")
    public ResultWrapper<List<SuperCategoryVO>> getSuperCategories() {
        return ResultWrapper.normal(indexService.getSuperCategories());
    }

    // 获取属于指定一级分类下所有的商品
    @Operation(summary = "获取属于指定一级分类下所有的商品")
    @PostMapping("/getProductsUnderMainCategory")
    public ResultWrapper<PageVO<ProductUnderMainCategoryVO>> getProductsUnderMainCategory(@Validated @RequestBody ProductUnderMainCategoryWithFilterDTO productUnderMainCategoryDTO) {
        return ResultWrapper.normal(indexService.getProductsUnderMainCategory(productUnderMainCategoryDTO.getProductUnderMainCategoryDTO(),
                productUnderMainCategoryDTO.getCategoryFilterDTO()));
    }

    // 二级分类
    @Operation(summary = "轮播图上方悬浮二级分类")
    @GetMapping("/getSubCategories")
    public ResultWrapper<List<SubCategoryVO>> getSubCategories(@NotNull Long mainCategoryId) {
        return ResultWrapper.normal(indexService.getSubCategories(mainCategoryId));
    }

    // 获取属于指定二级分类下所有的商品
    @Operation(summary = "获取属于指定二级分类下所有的商品")
    @PostMapping("/getProductsUnderSubCategory")
    public ResultWrapper<PageVO<ProductUnderSubCategoryVO>> getProductsUnderSubCategory(@Validated  @RequestBody ProductUnderSubCategoryWithFilterDTO productUnderSubCategoryWithFilterDTO) {
        return ResultWrapper.normal(indexService.getProductsUnderSubCategory(productUnderSubCategoryWithFilterDTO.getProductUnderSubCategoryDTO(),
                productUnderSubCategoryWithFilterDTO.getCategoryFilterDTO()));
    }

    // 最近上新栏
    @Operation(summary = "最近上新栏目")
    @GetMapping("/getLatestProducts")
    public ResultWrapper<List<LatestProductsVO>> getLatestProducts() {
        return ResultWrapper.normal(indexService.getLatestProducts());
    }

    // 精品推荐栏目
    @Operation(summary = "猜你喜欢 - 基于协同过滤（余弦相似度）")
    @GetMapping("/getRecommendations")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<List<RecommendProductVO>> getRecommendations(
            @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(indexService.getRecommendations(userId, Constants.DEFAULT_TOP_N));
    }
}
