package org.epsda.pets.repository;

import org.epsda.pets.pojo.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 21:38
 *
 * @Author: 憨八嘎
 */
@Repository
public interface PostRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findAllByPostIdIn(List<Long> postIds);
    PostDocument findPostDocumentByPostId(Long postId);
    Page<PostDocument> findPostDocumentByUserId(Long userId, Pageable pageable);
    Page<PostDocument> findPostDocumentByStatusAndUserId(Integer status, Long userId, Pageable pageable);
}
