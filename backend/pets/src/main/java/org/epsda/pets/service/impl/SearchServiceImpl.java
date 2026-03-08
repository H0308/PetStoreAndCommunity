package org.epsda.pets.service.impl;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.document.PostDocument;
import org.epsda.pets.pojo.document.ProductDocument;
import org.epsda.pets.pojo.dto.SearchListDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SearchListVO;
import org.epsda.pets.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 17:45
 *
 * @Author: 憨八嘎
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public PageVO<SearchListVO> search(SearchListDTO searchListDTO) {
        if (searchListDTO == null) {
            throw new PetException("搜索信息错误");
        }

        Long currentPage = searchListDTO.getCurrentPage();
        Long pageSize = searchListDTO.getPageSize();

        String keyword = searchListDTO.getKeyword();
        List<ProductDocument> productDocuments = this.searchProduct(keyword);
        List<PostDocument> postDocuments = this.searchPost(keyword);
        List<SearchListVO> searchListVOS = new ArrayList<>();
        productDocuments.forEach(productDocument ->
                searchListVOS.add(this.buildSearchListVOFromProductDocument(productDocument)));
        postDocuments.forEach(postDocument ->
                searchListVOS.add(this.buildSearchListVOFromPostDocument(postDocument)));

        // 计算分页参数
        int totalCount = searchListVOS.size();
        // 正确计算总页数（向上取整）
        long totalPages = (long) Math.ceil((double) totalCount / pageSize);
        // 计算起始和结束索引
        int startIndex = (int) ((currentPage - 1) * pageSize);
        int endIndex = (int) Math.min(startIndex + pageSize, totalCount);

        // 处理边界情况
        List<SearchListVO> resultList;
        if (startIndex >= totalCount) {
            // 请求的页码超出范围，返回空列表
            resultList = new ArrayList<>();
        } else {
            resultList = searchListVOS.subList(startIndex, endIndex);
        }

        return PageVO.<SearchListVO>builder()
                .currentPage(currentPage)
                .totalPages(totalPages)
                .totalCount((long) searchListVOS.size())
                .totalRecords(resultList)
                .build();
    }


    public List<ProductDocument> searchProduct(String keyword) {
        String fieldName = (keyword.length() <= 1)
                ? "productName.ngram"
                : "productName";

        // 构建match查询并设置fieldName
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                        .match(matchQuery -> matchQuery
                                .field(fieldName) // 使用动态字段名
                                .query(keyword)))
                .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                .build();
        // 进行查询
        SearchHits<ProductDocument> search = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
        return search.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public List<PostDocument> searchPost(String keyword) {
        String fieldName = (keyword.length() <= 1)
                ? "title.ngram"
                : "title";

        // 构建match查询并设置fieldName
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                    .bool(bool -> bool
                        .must(must -> must
                            .match(match -> match
                                .field(fieldName)
                                .query(keyword)))
                            .filter(filter -> filter
                                .term(term -> term
                                    .field("status")
                                        .value(Constants.POST_AUDIT_PASS)))))
                .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                .build();
        // 进行查询
        SearchHits<PostDocument> search = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        return search.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    public SearchListVO buildSearchListVOFromProductDocument(ProductDocument productDocument) {
        if (productDocument == null) {
            return null;
        }

        return SearchListVO.builder().objectId(productDocument.getId())
                .objectType(Constants.PRODUCT).subType(productDocument.getProductType())
                .objectName(productDocument.getProductName())
                .objectImageUrl(productDocument.getProductImageUrl())
                .objectMediaType(Constants.MEDIA_TYPE_IMAGE)
                .createTime(productDocument.getCreateTime())
                .build();
    }

    public SearchListVO buildSearchListVOFromPostDocument(PostDocument postDocument) {
        if (postDocument == null) {
            return null;
        }

        return SearchListVO.builder().objectId(postDocument.getPostId())
                .objectType(Constants.POST).subType(null)
                .objectName(postDocument.getTitle())
                .objectImageUrl(postDocument.getPostMediaUrl())
                .objectMediaType(postDocument.getPostMediaType())
                .createTime(postDocument.getCreateTime())
                .build();
    }
}
