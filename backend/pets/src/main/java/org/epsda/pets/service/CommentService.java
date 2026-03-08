package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.BaseCommentTextDTO;
import org.epsda.pets.pojo.dto.ProductCommentTextDTO;
import org.epsda.pets.pojo.vo.BaseCommentVO;
import org.epsda.pets.pojo.vo.CommentTextVO;
import org.epsda.pets.pojo.vo.ProductCommentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 20:25
 *
 * @Author: 憨八嘎
 */
public interface CommentService {
    List<ProductCommentVO> getProductComment(Long productId);

    CommentTextVO postProductComment(ProductCommentTextDTO productCommentTextDTO, List<MultipartFile> files);

    Boolean deleteProductComment(Long commentId, Long userId);

    List<BaseCommentVO> getPostComment(Long postId);

    CommentTextVO postPostComment(BaseCommentTextDTO baseCommentTextDTO, List<MultipartFile> files);

    Boolean deletePostComment(Long commentId, Long userId);
}
