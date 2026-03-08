package org.epsda.pets.controller.admin.home;

import io.swagger.v3.oas.annotations.Operation;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.bo.NewUserCountBO;
import org.epsda.pets.pojo.vo.HotTopicVO;
import org.epsda.pets.pojo.vo.OutOfStockProductBO;
import org.epsda.pets.pojo.vo.TopTotalPriceProductVO;
import org.epsda.pets.service.AdminIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/26
 * Time: 10:15
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/index")
public class AdminIndexController {

    @Autowired
    private AdminIndexService adminIndexService;

    // 获取当前系统用户数量
    @Operation(summary = "获取当前系统用户数量")
    @GetMapping("/userCount/{userId}")
    public ResultWrapper<Long> userCount(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.userCount(userId));
    }

    // 获取系统中的商品数量
    @Operation(summary = "获取系统中的商品数量")
    @GetMapping("/productCount/{userId}")
    public ResultWrapper<Long> productCount(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.productCount(userId));
    }

    // 成功交易的订单数量
    @Operation(summary = "成功交易的订单数量")
    @GetMapping("/orderCount/{userId}")
    public ResultWrapper<Long> orderCount(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.orderCount(userId));
    }

    // 获取总交易金额
    @Operation(summary = "获取总交易金额")
    @GetMapping("/orderPrice/{userId}")
    public ResultWrapper<BigDecimal> orderPrice(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.orderPrice(userId));
    }

    // 总销售金额占比最高的商品（前10，饼图）
    @Operation(summary = "总销售金额占比最高的商品")
    @GetMapping("/topTotalPrice/{userId}")
    public ResultWrapper<List<TopTotalPriceProductVO>> topTotalPrice(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.topTotalPrice(userId));
    }

    // 用户增长量（日统计，统计最近七天，折线图）
    @Operation(summary = "用户增长量")
    @GetMapping("/newUserCount/{userId}")
    public ResultWrapper<List<NewUserCountBO>> newUserCount(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.newUserCount(userId));
    }

    // 热门话题排行（前5，横向柱状图）
    @Operation(summary = "热门话题排行")
    @GetMapping("/hotTopic/{userId}")
    public ResultWrapper<List<HotTopicVO>> hotTopic(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.hotTopic(userId));
    }

    // 获取当前缺货的商品（表格，后续需要拓展“跳转到商品管理并携带上状态为缺货的筛选”）
    @Operation(summary = "获取当前缺货的商品")
    @GetMapping("/outOfStock/{userId}")
    public ResultWrapper<List<OutOfStockProductBO>> outOfStock(@PathVariable Long userId) {
        return ResultWrapper.normal(adminIndexService.outOfStock(userId));
    }
}
