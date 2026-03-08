package org.epsda.pets.repository;

import org.epsda.pets.pojo.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 19:45
 *
 * @Author: 憨八嘎
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {
    ProductDocument findProductDocumentById(Long id);
}
