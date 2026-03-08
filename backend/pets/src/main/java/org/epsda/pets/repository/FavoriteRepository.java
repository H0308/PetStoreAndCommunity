package org.epsda.pets.repository;

import org.epsda.pets.pojo.document.FavoriteDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/10
 * Time: 13:16
 *
 * @Author: 憨八嘎
 */
@Repository
public interface FavoriteRepository extends ElasticsearchRepository<FavoriteDocument, Long> {
    List<FavoriteDocument> findAllByPostIdIn(List<Long> postIds);
    FavoriteDocument findFavoriteDocumentByPostId(Long postId);
    Page<FavoriteDocument> findFavoriteDocumentByFavorUserId(Long favorUserId, Pageable pageable);
}
