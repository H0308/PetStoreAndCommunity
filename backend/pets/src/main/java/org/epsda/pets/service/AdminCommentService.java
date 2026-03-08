package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CommentListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.ReplyListVO;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/13
 * Time: 16:21
 *
 * @Author: 憨八嘎
 */
public interface AdminCommentService {
    PageVO<CommentListVO> list(CommentListDTO commentListDTO, CommentListFilterDTO commentListFilterDTO);

    PageVO<ReplyListVO> getReply(ReplyListDTO replyListDTO);

    Boolean delete(CommentDeleteDTO commentDeleteDTO);

    Boolean batchDelete(CommentBatchDeleteDTO commentBatchDeleteDTO);

    Boolean verify(CommentVerifyDTO commentVerifyDTO);
}
