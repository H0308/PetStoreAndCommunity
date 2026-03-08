package org.epsda.pets.controller.user.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.BaseCommentTextDTO;
import org.epsda.pets.pojo.dto.ProductCommentTextDTO;
import org.epsda.pets.pojo.vo.BaseCommentVO;
import org.epsda.pets.pojo.vo.CommentTextVO;
import org.epsda.pets.pojo.vo.ProductCommentVO;
import org.epsda.pets.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 19:50
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/comment")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 获取指定商品下的评论
    @Operation(summary = "获取指定商品下的评论")
    @GetMapping("/getProductComment")
    public ResultWrapper<List<ProductCommentVO>> getProductComment(@NotNull Long productId) {
        return ResultWrapper.normal(commentService.getProductComment(productId));
    }

    // 获取指定帖子的评论
    @Operation(summary = "获取指定帖子的评论")
    @GetMapping("/getPostComment")
    public ResultWrapper<List<BaseCommentVO>> getPostComment(@NotNull Long postId) {
        return ResultWrapper.normal(commentService.getPostComment(postId));
    }

    // 在指定商品下发布评论
    @Operation(summary = "在指定商品下发布评论")
    @PostMapping(value = "/postProductComment")
    public ResultWrapper<CommentTextVO> postProductComment(@Validated @RequestPart("content") ProductCommentTextDTO productCommentTextDTO,
                                                           @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResultWrapper.normal(commentService.postProductComment(productCommentTextDTO, files));
    }

    // 在指定帖子下发布评论
    @Operation(summary = "在指定帖子下发布评论")
    @PostMapping(value = "/postPostComment")
    public ResultWrapper<CommentTextVO> postPostComment(@Validated @RequestPart("content") BaseCommentTextDTO baseCommentTextDTO,
                                                        @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResultWrapper.normal(commentService.postPostComment(baseCommentTextDTO, files));
    }

    // 删除评论
    @Operation(summary = "删除商品评论")
    @PostMapping("/deleteProductComment")
    public ResultWrapper<Boolean> deleteProductComment(@NotNull @RequestParam("commentId") Long commentId,
                                                       @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(commentService.deleteProductComment(commentId, userId));
    }

    @Operation(summary = "删除帖子评论")
    @PostMapping("/deletePostComment")
    public ResultWrapper<Boolean> deletePostComment(@NotNull @RequestParam("commentId") Long commentId,
                                                    @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(commentService.deletePostComment(commentId, userId));
    }
}
