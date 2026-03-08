package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.document.ProductDocument;
import org.epsda.pets.pojo.vo.BaseProductDetailVO;
import org.epsda.pets.repository.ProductRepository;
import org.epsda.pets.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 11:16
 *
 * @Author: 憨八嘎
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private ProductCategorySuperMapper productCategorySuperMapper;
    @Autowired
    private ProductCategorySubMapper productCategorySubMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void buildProductDataIntoES() {
        // 获取到未下架且未删除的商品
        List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG).ne(ProductSuper::getStatus, Constants.OFF_SHELF));

        // 将数据存储构建并存储到ES中
        List<ProductDocument> productDocuments = new ArrayList<>();
        productSupers.forEach(product ->
                productDocuments.add(this.buildProductDocumentFromProductSuper(product)));
        productRepository.saveAll(productDocuments);
    }

    @Override
    public BaseProductDetailVO getBaseDetail(ProductSuper productSuper) {
        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(productSuper.getId());
        List<String> productImagesUrl = new ArrayList<>();
        productImages.forEach(productImage -> productImagesUrl.add(productImage.getImageUrl()));
        // 获取分类
        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectCategoryByCategoryId(productSuper.getMainCategoryId());
        ProductCategorySub productCategorySub = productCategorySubMapper.selectSubCategoryByCategoryId(productSuper.getSubCategoryId());
        // 获取发货地址
        Address address = addressMapper.selectById(productSuper.getShipId());

        return BaseProductDetailVO.builder()
                .productId(productSuper.getId())
                .identifier(productSuper.getIdentifier())
                .productType(productSuper.getType())
                .deliverAddress(address.getAddressText())
                .name(productSuper.getName())
                .description(productSuper.getDescription())
                .status(productSuper.getStatus())
                .stock(productSuper.getStock())
                .mainCategoryName(productCategorySuper.getName())
                .subCategoryName(productCategorySub.getName())
                .price(productSuper.getPrice())
                .productImages(productImagesUrl)
                .build();
    }

    public ProductDocument buildProductDocumentFromProductSuper(ProductSuper productSuper) {
        if (productSuper == null) {
            return null;
        }

        Long id = productSuper.getId();
        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(id);
        String productImageUrl = productImages.get(0).getImageUrl();

        return ProductDocument.builder()
                .id(id).productName(productSuper.getName())
                .productType(productSuper.getType())
                .productImageUrl(productImageUrl)
                .createTime(productSuper.getCreateTime())
                .build();
    }

}
