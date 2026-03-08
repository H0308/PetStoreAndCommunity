package org.epsda.pets.controller.admin.post;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.ColumnListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.TopicListVO;
import org.epsda.pets.service.AdminColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 17:29
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/column")
public class AdminColumnController {

    @Autowired
    private AdminColumnService adminColumnService;

    // 获取所有栏目
    @Operation(summary = "获取所有栏目")
    @PostMapping("/list")
    public ResultWrapper<PageVO<ColumnListVO>> list(@Validated @RequestBody ColumnListWithFilterDTO columnListWithFilterDTO) {
        return ResultWrapper.normal(adminColumnService.list(columnListWithFilterDTO.getColumnListDTO(),
                columnListWithFilterDTO.getColumnListFilterDTO()));
    }

    // 修改栏目
    @Operation(summary = "修改栏目")
    @PostMapping("change")
    public ResultWrapper<Boolean> change(@Validated @RequestBody ColumnChangeDTO columnChangeDTO) {
        return ResultWrapper.normal(adminColumnService.change(columnChangeDTO));
    }

    // 新增栏目
    @Operation(summary = "新增栏目")
    @PostMapping("/add")
    public ResultWrapper<Boolean> add(@Validated @RequestBody ColumnAddDTO columnAddDTO) {
        return ResultWrapper.normal(adminColumnService.add(columnAddDTO));
    }

    // 删除栏目
    @Operation(summary = "删除栏目")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("columnId") Long columnId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminColumnService.delete(columnId, userId));
    }

    // 批量删除栏目
    @Operation(summary = "批量删除栏目")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("columnIds") List<Long> columnIds,
                                              @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminColumnService.batchDelete(columnIds, userId));
    }
}
