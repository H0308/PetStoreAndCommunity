package org.epsda.pets.controller.admin.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.PostBatchDeleteDTO;
import org.epsda.pets.pojo.dto.PostListWithFilterDTO;
import org.epsda.pets.pojo.dto.PostVerifyDTO;
import org.epsda.pets.pojo.vo.AdminPostDetailVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.PostListVO;
import org.epsda.pets.service.AdminPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 20:37
 *
 * @Author: 憨八嘎
 */
@RequestMapping("/api/admin/post")
@RestController
public class AdminPostController {

    @Autowired
    private AdminPostService adminPostService;

    // 获取所有用户的帖子（包括管理员自己的）
    @Operation(summary = "获取所有用户的帖子")
    @PostMapping("/list")
    public ResultWrapper<PageVO<PostListVO>> list(@Validated @RequestBody PostListWithFilterDTO postListWithFilterDTO) {
        return ResultWrapper.normal(adminPostService.list(postListWithFilterDTO.getPostListDTO(),
                postListWithFilterDTO.getPostListFilterDTO()));
    }

    // 获取帖子详情接口
    @Operation(summary = "获取帖子详情接口")
    @PostMapping("/detail")
    public ResultWrapper<AdminPostDetailVO> detail(@NotNull @RequestParam("postId") Long postId,
                                                   @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminPostService.detail(postId, userId));
    }

    // 删除自己的帖子或者用户的帖子
    @Operation(summary = "删除自己的帖子或者用户的帖子")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("postId") Long postId,
                                         @NotNull @RequestParam("postUserId") Long postUserId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminPostService.delete(postId, postUserId, userId));
    }

    // 批量删除自己的帖子或者用户的帖子
    @Operation(summary = "删除自己的帖子或者用户的帖子")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@Validated @RequestBody PostBatchDeleteDTO postBatchDeleteDTO) {
        return ResultWrapper.normal(adminPostService.batchDelete(postBatchDeleteDTO));
    }

    // 人工审核帖子
    @Operation(summary = "人工审核帖子")
    @PostMapping("/verify")
    public ResultWrapper<Boolean> verify(@Validated @RequestBody PostVerifyDTO postVerifyDTO) {
        return ResultWrapper.normal(adminPostService.verify(postVerifyDTO));
    }
}
