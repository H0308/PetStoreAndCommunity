package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.AdminPostDetailVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.PostListVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/08
 * Time: 19:55
 *
 * @Author: 憨八嘎
 */
public interface AdminPostService {
    PageVO<PostListVO> list(PostListDTO postListDTO, PostListFilterDTO postListFilterDTO);

    AdminPostDetailVO detail(Long postId, Long userId);

    Boolean delete(Long postId, Long postUserId, Long userId);

    Boolean batchDelete(PostBatchDeleteDTO postBatchDeleteDTO);

    Boolean verify(PostVerifyDTO postVerifyDTO);
}
