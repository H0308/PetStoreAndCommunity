package org.epsda.pets.controller.user.profile;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.ProfilePasswordDTO;
import org.epsda.pets.pojo.dto.ProfileReceiptAddressDTO;
import org.epsda.pets.pojo.dto.RealNameAuthDTO;
import org.epsda.pets.pojo.dto.UserProfileChangeDTO;
import org.epsda.pets.pojo.vo.ProfileReceiptAddressVO;
import org.epsda.pets.pojo.vo.UserProfileChangeVO;
import org.epsda.pets.pojo.vo.UserProfileInfoVO;
import org.epsda.pets.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 10:33
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/profile")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // 获取个人信息
    @Operation(summary = "获取个人信息")
    @GetMapping("/getInfo")
    public ResultWrapper<UserProfileInfoVO> getInfo(@NotNull Long userId) {
        return ResultWrapper.normal(profileService.getInfo(userId));
    }

    // 修改个人信息
    @Operation(summary = "修改个人信息")
    @PostMapping("/changeInfo")
    public ResultWrapper<UserProfileChangeVO> changeInfo(@Validated @RequestBody UserProfileChangeDTO userProfileChangeDTO) {
        return ResultWrapper.normal(profileService.changeInfo(userProfileChangeDTO));
    }

    // 上传头像
    @Operation(summary = "上传头像")
    @PostMapping("/uploadAvatar")
    public ResultWrapper<String> uploadAvatar(@NotNull @RequestParam("userId") Long userId,
                                               @NotNull @RequestPart("file") MultipartFile file) {
        return ResultWrapper.normal(profileService.uploadAvatar(userId, file));
    }

    // 进行实名认证
    @Operation(summary = "进行实名认证")
    @PostMapping("/realNameAuth")
    public ResultWrapper<Boolean> realNameAuth(@Validated @RequestBody RealNameAuthDTO realNameAuthDTO) {
        return ResultWrapper.normal(profileService.realNameAuth(realNameAuthDTO));
    }

    // 获取快递地址
    @Operation(summary = "获取快递地址")
    @GetMapping("/getReceiptAddress")
    public ResultWrapper<ProfileReceiptAddressVO> getReceiptAddress(@NotNull Long userId) {
        return ResultWrapper.normal(profileService.getReceiptAddress(userId));
    }

    // 修改快递地址
    @Operation(summary = "修改快递地址")
    @PostMapping("/changeReceiptAddress")
    public ResultWrapper<ProfileReceiptAddressVO> changeReceiptAddress(@Validated @RequestBody ProfileReceiptAddressDTO profileReceiptAddressDTO) {
        return ResultWrapper.normal(profileService.changeReceiptAddress(profileReceiptAddressDTO));
    }

    // 修改账号密码
    @Operation(summary = "修改账号密码")
    @PostMapping("/changePassword")
    public ResultWrapper<Boolean> changePassword(@Validated @RequestBody ProfilePasswordDTO profilePasswordDTO) {
        return ResultWrapper.normal(profileService.changePassword(profilePasswordDTO));
    }

    // 获取手机号
    @Operation(summary = "获取手机号")
    @GetMapping("/getPhone")
    public ResultWrapper<String> getPhone(@NotNull Long userId) {
        return ResultWrapper.normal(profileService.getPhone(userId));
    }

    // 修改手机号
    @Operation(summary = "修改手机号")
    @PostMapping("/changePhone")
    public ResultWrapper<String> changePhone(@NotNull @RequestParam("userId") Long userId,
                                             @NotNull @RequestParam("phone") String phone) {
        return ResultWrapper.normal(profileService.changePhone(userId, phone));
    }

    // 获取邮箱
    @Operation(summary = "获取邮箱")
    @GetMapping("/getEmail")
    public ResultWrapper<String> getEmail(@NotNull Long userId) {
        return ResultWrapper.normal(profileService.getEmail(userId));
    }

    // 修改邮箱
    @Operation(summary = "修改邮箱")
    @PostMapping("/changeEmail")
    public ResultWrapper<String> changeEmail(@NotNull @RequestParam("userId") Long userId,
                                             @NotNull @RequestParam("email") String email) {
        return ResultWrapper.normal(profileService.changeEmail(userId, email));
    }

    // 注销账号
    @Operation(summary = "注销账号")
    @PostMapping("/deactivateAccount")
    public ResultWrapper<Boolean> deactivateAccount(@NotNull Long userId) {
        return ResultWrapper.normal(profileService.deactivateAccount(userId));
    }
}
