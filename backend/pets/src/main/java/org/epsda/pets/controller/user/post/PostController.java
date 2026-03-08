package org.epsda.pets.controller.user.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/18
 * Time: 10:25
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/post")
public class PostController {

    @Autowired
    private PostService postService;

    // 获取所有帖子
    @Operation(summary = "获取所有帖子")
    @PostMapping("/list")
    public ResultWrapper<PageVO<PostVO>> list(@RequestBody PostsDTO postsDTO) {
        return ResultWrapper.normal(postService.list(postsDTO));
    }

    // 获取所有栏目
    @Operation(summary = "获取所有栏目")
    @GetMapping("/column")
    public ResultWrapper<List<PostColumnInfosVO>> column() {
        return ResultWrapper.normal(postService.column());
    }

    // 获取所有话题
    @Operation(summary = "获取所有话题")
    @GetMapping("/topic")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<List<PostTopicInfosVO>> topic() {
        return ResultWrapper.normal(postService.topic());
    }

    // 获取用户自己的帖子
    @Operation(summary = "获取用户自己的帖子")
    @PostMapping("/getFromUser")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PageVO<SelfPostVO>> getFromUser(@Validated @RequestBody SelfPostsWithFilterDTO selfPostsWithFilterDTO) {
        return ResultWrapper.normal(postService.getFromUser(selfPostsWithFilterDTO.getSelfPostDTO(), selfPostsWithFilterDTO.getSelfPostFilterDTO()));
    }

    // 获取指定帖子的详情
    @Operation(summary = "获取指定帖子的详情")
    @PostMapping("/detail")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PostDetailVO> detail(@NotNull Long postId) {
        return ResultWrapper.normal(postService.detail(postId));
    }

    // 获取指定栏目的帖子
    @Operation(summary = "获取指定栏目的帖子")
    @PostMapping("/getUnderColumn")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PageVO<PostVO>> getUnderColumn(@Validated @RequestBody PostUnderColumnDTO postUnderColumnDTO) {
        return ResultWrapper.normal(postService.getUnderColumn(postUnderColumnDTO));
    }

    // 获取指定话题下的帖子
    @Operation(summary = "获取指定话题下的帖子")
    @PostMapping("/getUnderTopic")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PageVO<PostVO>> getUnderTopic(@Validated @RequestBody PostUnderTopicDTO postUnderTopicDTO) {
        return ResultWrapper.normal(postService.getUnderTopic(postUnderTopicDTO));
    }

    // 上传帖子
    @Operation(summary = "上传帖子")
    @PostMapping("/toPost")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PostTextVO> toPost(@Validated @RequestPart("content") ToPostDTO toPostDTO,
                                            @NotNull @RequestParam("mediaType") Integer mediaType,
                                            @NotNull @RequestPart("files") List<MultipartFile> files) {
        return ResultWrapper.normal(postService.toPost(toPostDTO, mediaType, files));
    }

    // 修改帖子
    @Operation(summary = "修改帖子")
    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PostEditVO> edit(@Validated @RequestPart("content") PostEditDTO postEditDTO,
                                          @RequestParam(value = "mediaType", required = false) Integer mediaType,
                                          @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResultWrapper.normal(postService.edit(postEditDTO, mediaType, files));
    }

    // 获取指定用户针对指定帖子的点赞状态
    @Operation(summary = "获取指定用户针对指定帖子的点赞状态")
    @PostMapping("/getLike")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Boolean> getLike(@NotNull @RequestParam("userId") Long userId,
                                          @NotNull @RequestParam("postId") Long postId) {
        return ResultWrapper.normal(postService.getLike(userId, postId));
    }

    // 获取指定用户针对指定帖子的点踩状态
    @Operation(summary = "获取指定用户针对指定帖子的点踩状态")
    @PostMapping("/getDislike")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Boolean> getDislike(@NotNull @RequestParam("userId") Long userId,
                                             @NotNull @RequestParam("postId") Long postId) {
        return ResultWrapper.normal(postService.getDislike(userId, postId));
    }

    // 给指定帖子点赞或者取消
    @Operation(summary = "给指定帖子点赞或者取消")
    @PostMapping("/like")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Long> like(@Validated @RequestBody PostLikeDTO postLikeDTO) {
        return ResultWrapper.normal(postService.like(postLikeDTO));
    }

    // 给指定帖子点踩或者取消
    @Operation(summary = "给指定帖子点踩或者取消")
    @PostMapping("/dislike")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Long> dislike(@Validated @RequestBody PostDislikeDTO postDislikeDTO) {
        return ResultWrapper.normal(postService.dislike(postDislikeDTO));
    }

    // 获取收藏的帖子
    @Operation(summary = "获取收藏的帖子")
    @PostMapping("/favorite")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<PageVO<FavoritePostVO>> favorite(@Validated @RequestBody FavoritePostDTO favoritePostDTO) {
        return ResultWrapper.normal(postService.favorite(favoritePostDTO));
    }

    // 获取指定帖子在指定用户下的状态
    @Operation(summary = "获取指定帖子在指定用户下的状态")
    @GetMapping("/getFavor")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Boolean> getFavor(@NotNull @RequestParam("userId") Long userId,
                                           @NotNull @RequestParam("postId") Long postId) {
        return ResultWrapper.normal(postService.getFavor(userId, postId));
    }

    // 取消收藏/收藏帖子
    @Operation(summary = "取消收藏/收藏帖子")
    @PostMapping("/favor")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Long> favor(@Validated @RequestBody FavorPostDTO favorPostDTO) {
        return ResultWrapper.normal(postService.favor(favorPostDTO));
    }

    // 用户删除自己发布的帖子
    @Operation(summary = "用户删除自己发布的帖子")
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("userId") Long userId,
                                         @NotNull @RequestParam("postId") Long postId) {
        return ResultWrapper.normal(postService.delete(userId, postId));
    }

    // 帖子推荐
    @Operation(summary = "帖子推荐")
    @GetMapping("/getRecommendations")
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    public ResultWrapper<List<PostVO>> getRecommendations(
            @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(postService.getRecommendations(userId, Constants.DEFAULT_TOP_N));
    }
}
