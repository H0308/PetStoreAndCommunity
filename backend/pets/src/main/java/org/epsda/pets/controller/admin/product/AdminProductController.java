package org.epsda.pets.controller.admin.product;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.AdminProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 17:43
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;

    // 获取所有商品
    @Operation(summary = "获取所有商品")
    @PostMapping("/list")
    public ResultWrapper<PageVO<ProductListVO>> list(@Validated @RequestBody ProductListWithFilterDTO productListWithFilterDTO) {
        return ResultWrapper.normal(adminProductService.list(productListWithFilterDTO.getProductListDTO(), productListWithFilterDTO.getProductFilterDTO()));
    }

    // 获取指定宠物商品信息用于修改
    @Operation(summary = "获取指定宠物商品信息用于修改")
    @PostMapping("/getPet")
    public ResultWrapper<AdminPetDetailVO> getPet(@NotNull @RequestParam("productId") Long productId,
                                     @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminProductService.getPet(productId, userId));
    }

    // 获取指定宠物用品信息用于修改
    @Operation(summary = "获取指定宠物商品信息用于修改")
    @PostMapping("/getSupply")
    public ResultWrapper<AdminSupplyDetailVO> getSupply(@NotNull @RequestParam("productId") Long productId,
                                                  @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminProductService.getSupply(productId, userId));
    }

    // 修改指定商品信息
    @Operation(summary = "修改指定商品信息")
    @PostMapping(value = "/change")
    public ResultWrapper<ProductChangeVO> change(@Validated @RequestPart("content") MainProductChangeDTO mainProductChangeDTO,
                                                 @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                 @RequestPart(value = "mainFlags", required = false) List<Integer> mainFlags) {
        return ResultWrapper.normal(adminProductService.change(mainProductChangeDTO, files, mainFlags));
    }

    // 新增宠物商品信息
    @Operation(summary = "新增宠物商品信息")
    @PostMapping(value = "/addPet")
    public ResultWrapper<ProductAddVO> addPet(@Validated @RequestPart("content") PetProductAddDTO petProductAddDTO,
                                              @RequestPart(value = "files") List<MultipartFile> files,
                                              @RequestPart(value = "mainFlags") List<Integer> mainFlags) {
        return ResultWrapper.normal(adminProductService.addPet(petProductAddDTO, files, mainFlags));
    }

    // 新增宠物用品信息
    @Operation(summary = "新增宠物用品信息")
    @PostMapping(value = "/addSupply")
    public ResultWrapper<ProductAddVO> addSupply(@Validated @RequestPart("content") SupplyProductAddDTO supplyProductAddDTO,
                                                 @RequestPart(value = "files") List<MultipartFile> files,
                                                 @RequestPart(value = "mainFlags") List<Integer> mainFlags) {
        return ResultWrapper.normal(adminProductService.addSupply(supplyProductAddDTO, files, mainFlags));
    }

    // 上架商品
    @Operation(summary = "上架商品")
    @PostMapping("/onSelling")
    public ResultWrapper<Boolean> onSelling(@NotNull @RequestParam("productId") Long productId, @RequestParam("userId") @NotNull Long userId) {
        return ResultWrapper.normal(adminProductService.onSelling(productId, userId));
    }

    // 下架商品
    @Operation(summary = "下架商品")
    @PostMapping("/offShelf")
    public ResultWrapper<Boolean> offShelf(@NotNull @RequestParam("productId") Long productId, @RequestParam("userId") @NotNull Long userId) {
        return ResultWrapper.normal(adminProductService.offShelf(productId, userId));
    }

    // 删除商品
    @Operation(summary = "删除商品")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("productId") Long productId, @RequestParam("userId") @NotNull Long userId) {
        return ResultWrapper.normal(adminProductService.delete(productId, userId));
    }

    // 批量删除商品（先对商品进行下架，再批量删除）
    @Operation(summary = "批量删除商品")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("productIds") List<Long> productIds,
                                              @RequestParam("userId") @NotNull Long userId) {
        return ResultWrapper.normal(adminProductService.batchDelete(productIds, userId));
    }
}
