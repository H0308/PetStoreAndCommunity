package org.epsda.pets.controller.user.product;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.vo.PetProductDetailVO;
import org.epsda.pets.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 16:04
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/product/pet")
public class PetController {
    @Autowired
    private PetService petService;

    // 获取指定宠物商品详细信息
    @Operation(summary = "获取指定宠物商品详细信息")
    @GetMapping("/getPetDetail")
    public ResultWrapper<PetProductDetailVO> getPetDetail(@NotNull Long productId) {
        return ResultWrapper.normal(petService.getPetDetail(productId));
    }
}
