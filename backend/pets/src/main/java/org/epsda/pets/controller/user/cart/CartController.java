package org.epsda.pets.controller.user.cart;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.AddToCartDTO;
import org.epsda.pets.pojo.dto.CartChangeDTO;
import org.epsda.pets.pojo.dto.CartDetailDTO;
import org.epsda.pets.pojo.vo.CartChangeVO;
import org.epsda.pets.pojo.vo.CartProductDetailVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.PreOrderDetailVO;
import org.epsda.pets.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/16
 * Time: 11:01
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/cart")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class CartController {

    @Autowired
    private CartService cartService;

    // 获取购物车商品数量
    @Operation(summary = "获取购物车商品数量")
    @GetMapping("/getCounts")
    public ResultWrapper<Long> counts(@NotNull Long userId) {
        return ResultWrapper.normal(cartService.getCounts(userId));
    }

    // 获取购物车列表
    @Operation(summary = "获取购物车列表")
    @PostMapping("/list")
    public ResultWrapper<PageVO<CartProductDetailVO>> list(@Validated @RequestBody CartDetailDTO cartDetailDTO) {
        return ResultWrapper.normal(cartService.list(cartDetailDTO));
    }

    // 商品加入购物车
    @Operation(summary = "商品加入购物车")
    @PostMapping("/add")
    public ResultWrapper<Boolean> add(@Validated @RequestBody AddToCartDTO addToCartDTO) {
        return ResultWrapper.normal(cartService.add(addToCartDTO));
    }

    // 修改购物车指定商品
    @Operation(summary = "修改购物车指定商品")
    @PostMapping("/change")
    public ResultWrapper<CartChangeVO> change(@Validated @RequestBody CartChangeDTO cartChangeDTO) {
        return ResultWrapper.normal(cartService.change(cartChangeDTO));
    }

    // 结算选中的商品
    @Operation(summary = "结算选中的商品")
    @PostMapping("/settle")
    public ResultWrapper<PreOrderDetailVO> settle(@NotNull Long cartId) {
        return ResultWrapper.normal(cartService.settle(cartId));
    }

    // 批量结算选中的商品
    @Operation(summary = "批量结算选中的商品")
    @PostMapping("/batchSettle")
    public ResultWrapper<List<PreOrderDetailVO>> batchSettle(@NotNull @RequestParam List<Long> cartIds) {
        return ResultWrapper.normal(cartService.batchSettle(cartIds));
    }

    // 删除购物车中指定商品
    @Operation(summary = "删除购物车中指定商品")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull Long cartId) {
        return ResultWrapper.normal(cartService.delete(cartId));
    }

    // 批量删除购物车中的商品
    @Operation(summary = "批量删除购物车中的商品")
    @PostMapping("/batchDelete")
    public ResultWrapper<List<Boolean>> batchDelete(@RequestParam List<Long> cartIds) {
        return ResultWrapper.normal(cartService.batchDelete(cartIds));
    }
}
