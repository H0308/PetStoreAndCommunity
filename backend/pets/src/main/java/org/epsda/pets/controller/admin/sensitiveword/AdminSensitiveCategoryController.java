package org.epsda.pets.controller.admin.sensitiveword;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.SensitiveCategoryAddDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryChangeDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListWithFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryListVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryVO;
import org.epsda.pets.service.AdminSensitiveCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 19:01
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/sensitiveCategory")
public class AdminSensitiveCategoryController {
    @Autowired
    private AdminSensitiveCategoryService adminSensitiveCategoryService;

    // 获取所有分类
    @Operation(summary = "获取所有分类")
    @PostMapping("/list")
    public ResultWrapper<PageVO<SensitiveCategoryListVO>> list(@Validated @RequestBody
                                   SensitiveCategoryListWithFilterDTO sensitiveCategoryListWithFilterDTO) {
        return ResultWrapper.normal(adminSensitiveCategoryService.list(
                sensitiveCategoryListWithFilterDTO.getSensitiveCategoryListDTO(),
                sensitiveCategoryListWithFilterDTO.getSensitiveCategoryListFilterDTO()));
    }

    // 获取所有分类供敏感词筛选、新增使用
    @Operation(summary = "获取所有分类供敏感词筛选、新增使用")
    @GetMapping("/getAll")
    public ResultWrapper<List<SensitiveCategoryVO>> getAll(@NotNull Long userId) {
        return ResultWrapper.normal(adminSensitiveCategoryService.getAll(userId));
    }

    // 新增分类
    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public ResultWrapper<Boolean> add(@Validated @RequestBody SensitiveCategoryAddDTO sensitiveCategoryAddDTO) {
        return ResultWrapper.normal(adminSensitiveCategoryService.add(sensitiveCategoryAddDTO));
    }

    // 修改分类
    @Operation(summary = "修改分类")
    @PostMapping("/change")
    public ResultWrapper<Boolean> change(@Validated @RequestBody SensitiveCategoryChangeDTO sensitiveCategoryChangeDTO) {
        return ResultWrapper.normal(adminSensitiveCategoryService.change(sensitiveCategoryChangeDTO));
    }

    // 删除分类
    @Operation(summary = "删除分类")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("categoryId") Long categoryId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminSensitiveCategoryService.delete(categoryId, userId));
    }

    // 批量删除分类
    @Operation(summary = "修改分类")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("categoryIds") List<Long> categoryIds,
                                              @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminSensitiveCategoryService.batchDelete(categoryIds, userId));
    }
}
