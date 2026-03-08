package org.epsda.pets.controller.user.product;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.vo.SupplyDetailProductVO;
import org.epsda.pets.service.SupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 10:24
 *
 * @Author: 憨八嘎
 */
@RequestMapping("/api/user/product/supply")
@RestController
public class SupplyController {

    @Autowired
    private SupplyService supplyService;

    @Operation(summary = "获取指定宠物用品商品详细信息")
    @GetMapping("/getSupplyDetail")
    public ResultWrapper<SupplyDetailProductVO> getSupplyDetail(@NotNull Long productId) {
        return ResultWrapper.normal(supplyService.getSupplyDetail(productId));
    }
}
