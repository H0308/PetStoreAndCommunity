package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/18
 * Time: 11:13
 *
 * @Author: 憨八嘎
 */
public interface PostService {
    PageVO<PostVO> list(PostsDTO postsDTO);

    List<PostColumnInfosVO> column();

    List<PostTopicInfosVO> topic();

    PageVO<SelfPostVO> getFromUser(SelfPostDTO selfPostDTO, SelfPostFilterDTO selfPostFilterDTO);

    PostDetailVO detail(Long postId);

    PageVO<PostVO> getUnderColumn(PostUnderColumnDTO postUnderColumnDTO);

    PageVO<PostVO> getUnderTopic(PostUnderTopicDTO postUnderTopicDTO);

    PostTextVO toPost(ToPostDTO toPostDTO, Integer mediaType, List<MultipartFile> files);

    PostEditVO edit(PostEditDTO postEditDTO, Integer mediaType, List<MultipartFile> files);

    Long like(PostLikeDTO postLikeDTO);

    Long dislike(PostDislikeDTO postDislikeDTO);

    Boolean getLike(Long userId, Long postId);

    Boolean getDislike(Long userId, Long postId);

    PageVO<FavoritePostVO> favorite(FavoritePostDTO favoritePostDTO);

    Long favor(FavorPostDTO favorPostDTO);

    Boolean getFavor(Long userId, Long postId);

    Boolean delete(Long userId, Long postId);

    List<PostVO> getRecommendations(Long userId, Integer defaultTopN);
}
