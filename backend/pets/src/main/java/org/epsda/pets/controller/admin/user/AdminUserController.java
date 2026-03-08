package org.epsda.pets.controller.admin.user;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.UserDetailChangeDTO;
import org.epsda.pets.pojo.dto.UserDetailListDTO;
import org.epsda.pets.pojo.dto.UserDetailFilterDTO;
import org.epsda.pets.pojo.dto.UserDetailListWithFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.UserDetailListVO;
import org.epsda.pets.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 10:29
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // 分页+筛选获取用户列表
    @Operation(summary = "分页获取用户列表")
    @PostMapping("/list")
    public ResultWrapper<PageVO<UserDetailListVO>> list(@Validated @RequestBody UserDetailListWithFilterDTO userDetailListWithFilterDTO) {
        return ResultWrapper.normal(adminUserService.list(userDetailListWithFilterDTO.getUserDetailListDTO(),
                userDetailListWithFilterDTO.getUserDetailFilterDTO()));
    }

    // 修改手机号、邮箱、用户收货信息和角色信息
    // 不修改个性化信息（头像、用户名等）
    @Operation(summary = "修改用户信息")
    @PostMapping("/change")
    public ResultWrapper<Boolean> change(@Validated @RequestBody UserDetailChangeDTO userDetailChangeDTO) {
        return ResultWrapper.normal(adminUserService.change(userDetailChangeDTO));
    }

    // 重置用户密码
    @Operation(summary = "重置用户密码")
    @PostMapping("/reset")
    public ResultWrapper<Boolean> reset(@NotNull @RequestParam("userIdChange") Long userIdChange,
                                        @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.reset(userIdChange, userId));
    }

    // 销户
    @Operation(summary = "销户")
    @PostMapping("/deactivate")
    public ResultWrapper<Boolean> deactivate(@NotNull @RequestParam("userIdChange") Long userIdChange,
                                        @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.deactivate(userIdChange, userId));
    }

    // 删除用户
    @Operation(summary = "删除用户")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("userIdChange") Long userIdChange,
                                             @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.delete(userIdChange, userId));
    }

    // 批量删除用户
    @Operation(summary = "批量删除用户")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("userIdChanges") List<Long> userIdChanges,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.batchDelete(userIdChanges, userId));
    }

    // 禁言用户
    @Operation(summary = "禁言用户")
    @PostMapping("/ban")
    public ResultWrapper<Boolean> ban(@NotNull @RequestParam("userIdChange") Long userIdChange,
                                              @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.ban(userIdChange, userId));
    }

    // 取消禁言
    @Operation(summary = "取消禁言用户")
    @PostMapping("/unBan")
    public ResultWrapper<Boolean> unBan(@NotNull @RequestParam("userIdChange") Long userIdChange,
                                      @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.unBan(userIdChange, userId));
    }

    // 批量禁言用户
    @Operation(summary = "批量禁言用户")
    @PostMapping("/batchBan")
    public ResultWrapper<Boolean> batchBan(@NotNull @RequestParam("userIdChanges") List<Long> userIdChanges,
                                      @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.batchBan(userIdChanges, userId));
    }

    @Operation(summary = "批量取消禁言用户")
    @PostMapping("/batchUnBan")
    public ResultWrapper<Boolean> batchUnBan(@NotNull @RequestParam("userIdChanges") List<Long> userIdChanges,
                                           @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminUserService.batchUnBan(userIdChanges, userId));
    }
}
