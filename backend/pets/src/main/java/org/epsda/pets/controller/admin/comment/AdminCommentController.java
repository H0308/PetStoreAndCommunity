package org.epsda.pets.controller.admin.comment;

import io.swagger.v3.oas.annotations.Operation;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CommentListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.ReplyListVO;
import org.epsda.pets.service.AdminCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/12
 * Time: 10:27
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    @Autowired
    private AdminCommentService adminCommentService;

    // 获取所有评论
    @Operation(summary = "获取所有评论")
    @PostMapping("/list")
    public ResultWrapper<PageVO<CommentListVO>> list(@Validated @RequestBody CommentListWithFilterDTO commentListWithFilterDTO) {
        return ResultWrapper.normal(adminCommentService.list(commentListWithFilterDTO.getCommentListDTO(),
                commentListWithFilterDTO.getCommentListFilterDTO()));
    }

    // 获取指定评论下的所有回复
    @Operation(summary = "获取指定评论下的所有回复")
    @PostMapping("/getReply")
    public ResultWrapper<PageVO<ReplyListVO>> getReply(@Validated @RequestBody ReplyListDTO replyListDTO) {
        return ResultWrapper.normal(adminCommentService.getReply(replyListDTO));
    }

    // 删除评论
    @Operation(summary = "删除评论")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@Validated @RequestBody CommentDeleteDTO commentDeleteDTO) {
        return ResultWrapper.normal(adminCommentService.delete(commentDeleteDTO));
    }

    // 批量删除评论
    @Operation(summary = "删除评论")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@Validated @RequestBody CommentBatchDeleteDTO commentBatchDeleteDTO) {
        return ResultWrapper.normal(adminCommentService.batchDelete(commentBatchDeleteDTO));
    }

    // 审核评论
    @Operation(summary = "审核评论")
    @PostMapping("/verify")
    public ResultWrapper<Boolean> verify(@Validated @RequestBody CommentVerifyDTO commentVerifyDTO) {
        return ResultWrapper.normal(adminCommentService.verify(commentVerifyDTO));
    }
}
