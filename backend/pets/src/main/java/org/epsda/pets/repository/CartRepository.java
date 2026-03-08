package org.epsda.pets.repository;

import org.epsda.pets.pojo.document.CartDocument;
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
 * Time: 10:30
 *
 * @Author: 憨八嘎
 */
@Repository
public interface CartRepository extends ElasticsearchRepository<CartDocument, Long> {
    CartDocument findCartDocumentByCartId(Long cartId);
    Page<CartDocument> findCartDocumentsByUserId(Long userId, Pageable pageable);
}
