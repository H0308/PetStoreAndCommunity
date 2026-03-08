package org.epsda.pets.controller.admin.product;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CategorySearchListVO;
import org.epsda.pets.pojo.vo.MainCategoryListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SubCategoryListVO;
import org.epsda.pets.service.AdminCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 11:12
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    @Autowired
    private AdminCategoryService adminCategoryService;

    // 按照分类名称搜索分类（数据库模糊查询）
    @Operation(summary = "按照分类名称搜索分类")
    @PostMapping("/search")
    public ResultWrapper<CategorySearchListVO> search(@Validated @RequestBody CategorySearchListDTO categorySearchListDTO) {
        return ResultWrapper.normal(adminCategoryService.search(categorySearchListDTO));
    }

    // 获取所有一级分类
    @Operation(summary = "获取所有一级分类")
    @PostMapping("/mainCategory")
    public ResultWrapper<PageVO<MainCategoryListVO>> mainCategory(@Validated @RequestBody MainCategoryListDTO mainCategoryListDTO) {
        return ResultWrapper.normal(adminCategoryService.mainCategory(mainCategoryListDTO));
    }

    // 获取一级分类下所有二级分类
    @Operation(summary = "获取一级分类下所有二级分类")
    @PostMapping("/subCategory")
    public ResultWrapper<PageVO<SubCategoryListVO>> subCategory(@Validated @RequestBody SubCategoryListDTO subCategoryListDTO) {
        return ResultWrapper.normal(adminCategoryService.subCategory(subCategoryListDTO));
    }

    // 新增一级分类
    @Operation(summary = "新增一级分类")
    @PostMapping("/addMainCategory")
    public ResultWrapper<Boolean> addMainCategory(@Validated @RequestBody MainCategoryAddDTO mainCategoryAddDTO) {
        return ResultWrapper.normal(adminCategoryService.addMainCategory(mainCategoryAddDTO));
    }

    // 新增二级分类
    @Operation(summary = "新增二级分类")
    @PostMapping("/addSubCategory")
    public ResultWrapper<Boolean> addSubCategory(@Validated @RequestBody SubCategoryDTO subCategoryDTO) {
        return ResultWrapper.normal(adminCategoryService.addSubCategory(subCategoryDTO));
    }

    // 修改一级分类名称
    @Operation(summary = "修改一级分类名称")
    @PostMapping("/changeMainCategory")
    public ResultWrapper<Boolean> changeMainCategory(@Validated @RequestBody MainCategoryChangeDTO mainCategoryChangeDTO) {
        return ResultWrapper.normal(adminCategoryService.changeMainCategory(mainCategoryChangeDTO));
    }

    // 修改二级分类
    @Operation(summary = "修改一级分类名称")
    @PostMapping("/changeSubCategory")
    public ResultWrapper<Boolean> changeSubCategory(@Validated @RequestBody SubCategoryChangeDTO subCategoryChangeDTO) {
        return ResultWrapper.normal(adminCategoryService.changeSubCategory(subCategoryChangeDTO));
    }

    // 删除一级分类
    @Operation(summary = "删除一级分类")
    @PostMapping("/deleteMainCategory")
    public ResultWrapper<Boolean> deleteMainCategory(@NotNull @RequestParam("mainCategoryId") Long mainCategoryId,
                                                     @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminCategoryService.deleteMainCategory(mainCategoryId, userId));
    }

    // 删除二级分类
    @Operation(summary = "删除二级分类")
    @PostMapping("/deleteSubCategory")
    public ResultWrapper<Boolean> deleteSubCategory(@NotNull @RequestParam("subCategoryId") Long subCategoryId,
                                                     @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminCategoryService.deleteSubCategory(subCategoryId, userId));
    }

    // 批量删除一级分类
    @Operation(summary = "批量删除一级分类")
    @PostMapping("/batchDeleteMainCategory")
    public ResultWrapper<Boolean> batchDeleteMainCategory(@NotNull @RequestParam("mainCategoryIds") List<Long> mainCategoryIds,
                                                          @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminCategoryService.batchDeleteMainCategory(mainCategoryIds, userId));
    }

    // 批量删除二级分类
    @Operation(summary = "批量删除二级分类")
    @PostMapping("/batchDeleteSubCategory")
    public ResultWrapper<Boolean> batchDeleteSubCategory(@NotNull @RequestParam("subCategoryIds") List<Long> subCategoryIds,
                                                          @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminCategoryService.batchDeleteSubCategory(subCategoryIds, userId));
    }
}
