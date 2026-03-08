package org.epsda.pets.controller.admin.sensitiveword;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.SensitiveWordAddDTO;
import org.epsda.pets.pojo.dto.SensitiveWordListWithFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveWordListVO;
import org.epsda.pets.service.AdminSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:32
 *
 * @Author: 憨八嘎
 */
@RequestMapping("/api/admin/sensitiveWord")
@RestController
public class AdminSensitiveWordController {

    @Autowired
    private AdminSensitiveWordService adminSensitiveWordService;

    // 获取所有敏感词
    @Operation(summary = "获取所有敏感词")
    @PostMapping("/list")
    public ResultWrapper<PageVO<SensitiveWordListVO>> list(@Validated @RequestBody SensitiveWordListWithFilterDTO sensitiveWordListWithFilterDTO) {
        return ResultWrapper.normal(adminSensitiveWordService.list(sensitiveWordListWithFilterDTO.getSensitiveWordListDTO(),
                sensitiveWordListWithFilterDTO.getSensitiveWordListFilterDTO()));
    }

    // 新增敏感词
    @Operation(summary = "新增敏感词")
    @PostMapping("/add")
    public ResultWrapper<Boolean> add(@Validated @RequestBody SensitiveWordAddDTO sensitiveWordAddDTO) {
        return ResultWrapper.normal(adminSensitiveWordService.add(sensitiveWordAddDTO));
    }

    // 通过CSV文件新增敏感词
    @Operation(summary = "通过CSV文件新增敏感词")
    @PostMapping("/addWithCSV")
    public ResultWrapper<Long> addWithCSV(@NotNull @RequestPart("file") MultipartFile file,
                                          @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminSensitiveWordService.addWithCSV(file, userId));
    }

    // 删除敏感词
    @Operation(summary = "删除敏感词")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("wordId") Long wordId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminSensitiveWordService.delete(wordId, userId));
    }

    // 批量删除敏感词
    @Operation(summary = "批量删除敏感词")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("wordIds") List<Long> wordIds,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminSensitiveWordService.batchDelete(wordIds, userId));
    }
}
