package org.epsda.pets.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.OrderCountBO;
import org.epsda.pets.pojo.dto.CategoryFilterDTO;
import org.epsda.pets.pojo.dto.ProductUnderMainCategoryDTO;
import org.epsda.pets.pojo.dto.ProductUnderSubCategoryDTO;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.IndexService;
import org.epsda.pets.utils.GenerateRandomIndices;
import org.epsda.pets.utils.JsonUtil;
import org.epsda.pets.utils.RecommendUtils;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 17:00
 *
 * @Author: 憨八嘎
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private ProductCategorySuperMapper productCategorySuperMapper;
    @Autowired
    private ProductCategorySubMapper productCategorySubMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CarouselVO> getCarousels() {
        // 从商品图片表中获取到所有标记为主图的图片
        List<ProductImage> productImages = productImageMapper.selectAllMainImage();
        if (productImages.size() < 3) {
            throw new PetException("主图数量不足3，无法构建轮播图");
        }

        List<Integer> indices = new ArrayList<>();
        indices.add(0);
        indices.add(1);
        indices.add(2);
        if (productImages.size() > 3) {
            indices = GenerateRandomIndices.generateRandomIndices(productImages.size(), 3);
        }

        List<CarouselVO> carouselVOS = new ArrayList<>();
        // 第一件商品信息
        ProductImage productImage_1 = productImages.get(indices.get(0));
        Long productId_1 = productImage_1.getProductId();
        ProductSuper productSuper_1 = productSuperMapper.selectById(productId_1);
        // 第二件商品信息
        ProductImage productImage_2 = productImages.get(indices.get(1));
        Long productId_2 = productImage_2.getProductId();
        ProductSuper productSuper_2 = productSuperMapper.selectById(productId_2);

        // 第三件商品信息
        ProductImage productImage_3 = productImages.get(indices.get(2));
        Long productId_3 = productImage_3.getProductId();
        ProductSuper productSuper_3 = productSuperMapper.selectById(productId_3);

        carouselVOS.add(CarouselVO.builder()
                .productId(productId_1)
                .imageUrl(productImage_1.getImageUrl())
                .productType(productSuper_1.getType())
                .build());
        carouselVOS.add(CarouselVO.builder()
                .productId(productId_2)
                .imageUrl(productImage_2.getImageUrl())
                .productType(productSuper_2.getType())
                .build());
        carouselVOS.add(CarouselVO.builder()
                .productId(productId_3)
                .imageUrl(productImage_3.getImageUrl())
                .productType(productSuper_3.getType())
                .build());

        return carouselVOS;
    }

    @Override
    public List<SuperCategoryVO> getSuperCategories() {
        String mainCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "main";
        // 先从缓存获取
        String mainCategoryRet = stringRedisTemplate.opsForValue().get(mainCategoryKey);
        if (mainCategoryRet != null) {
            if (!StringUtils.hasText(mainCategoryRet)) {
                throw new PetException("获取一级分类失败");
            }
            // 使用TypeReference防止泛型擦除为List，确保类型始终为List<SuperCategoryVO>
            return JsonUtil.toObject(mainCategoryRet,
                    new TypeReference<List<SuperCategoryVO>>() {});
        }

        // 获取到所有的宠物分类
        List<ProductCategorySuper> allCategories = productCategorySuperMapper.selectAllPetCategories();
        if (allCategories.isEmpty()) {
            // 防止缓存穿透
            stringRedisTemplate.opsForValue().set(mainCategoryKey, "");
            throw new PetException("获取一级分类失败");
        }

        List<SuperCategoryVO> superCategoryVOS = new ArrayList<>();
        allCategories.forEach(category ->
                        superCategoryVOS.add(SuperCategoryVO.builder()
                            .categoryId(category.getId())
                            .name(category.getName())
                            .build()));

        // 存储到缓存中
        String json = JsonUtil.toJson(superCategoryVOS);
        stringRedisTemplate.opsForValue().set(mainCategoryKey, json,
                Constants.INDEX_CATEGORY_EXPIRE_TIME_DAY, TimeUnit.DAYS);

        return superCategoryVOS;
    }

    @Override
    public List<SubCategoryVO> getSubCategories(Long mainCategoryId) {
        String subCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "sub:" + mainCategoryId;
        // 先从缓存获取
        String subCategoryRet = stringRedisTemplate.opsForValue().get(subCategoryKey);
        if (subCategoryRet != null) {
            if (!StringUtils.hasText(subCategoryRet)) {
                throw new PetException("获取二级分类失败");
            }
            // 使用TypeReference防止泛型擦除为List，确保类型始终为List<SubCategoryVO>
            return JsonUtil.toObject(subCategoryRet,
                    new TypeReference<List<SubCategoryVO>>() {});
        }


        // 根据指定的分类ID获取到所有二级分类
        List<ProductCategorySub> subCategories =
                productCategorySubMapper.selectAllSubCategoriesByMainCategoryId(mainCategoryId);

        if (subCategories.isEmpty()) {
            // 防止缓存穿透
            stringRedisTemplate.opsForValue().set(subCategoryKey, "",
                    Constants.NULL_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
            throw new PetException("当前没有根据指定一级分类ID获取到二级分类");
        }

        List<SubCategoryVO> subCategoryVOS = new ArrayList<>();
        subCategories.forEach(category ->
                subCategoryVOS.add(SubCategoryVO.builder()
                        .categoryId(category.getId())
                        .mainCategoryId(category.getMainCategoryId())
                        .name(category.getName())
                        .build()));

        // 存储到缓存中
        String json = JsonUtil.toJson(subCategoryVOS);
        stringRedisTemplate.opsForValue().set(subCategoryKey, json,
                Constants.INDEX_CATEGORY_EXPIRE_TIME_DAY, TimeUnit.DAYS);

        return subCategoryVOS;
    }

    @Override
    public List<LatestProductsVO> getLatestProducts() {
        // 根据创建时间降序获取到最近的商品
        List<ProductSuper> products = productSuperMapper.selectAllPetsByCreateTimeDesc();
        if (products.isEmpty()) {
            throw new PetException("无法获取到最近上新的商品");
        }
        List<LatestProductsVO> latestProductsVOS = new ArrayList<>();
        products.forEach(product -> latestProductsVOS.add(LatestProductsVO.builder()
                .productId(product.getId())
                .name(product.getName())
                .productType(product.getType())
                .imageUrl(productImageMapper.selectImagesByProductId(product.getId()).get(0).getImageUrl())
                .description(product.getDescription())
                .price(product.getPrice())
                .build()));

        return latestProductsVOS;
    }

    @Override
    public PageVO<ProductUnderMainCategoryVO> getProductsUnderMainCategory(ProductUnderMainCategoryDTO productUnderMainCategoryDTO,
                                                                             CategoryFilterDTO categoryFilterDTO) {
        // 构建分页对象
        Long currentPage = productUnderMainCategoryDTO.getCurrentPage();
        Long pageSize = productUnderMainCategoryDTO.getPageSize();
        Page<ProductSuper> productPage = new Page<>(currentPage, pageSize);

        // 获取筛选条件
        LambdaQueryWrapper<ProductSuper> productSuperLambdaQueryWrapper = buildPageFilterCondition(categoryFilterDTO);
        productSuperLambdaQueryWrapper.eq(ProductSuper::getMainCategoryId, productUnderMainCategoryDTO.getMainCategoryId())
                .ne(ProductSuper::getStatus, Constants.OFF_SHELF)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG);

        Page<ProductSuper> productSuperPage = productSuperMapper.selectPage(productPage, productSuperLambdaQueryWrapper);
        List<ProductUnderMainCategoryVO> productUnderMainCategoryVOS = new ArrayList<>();
        productSuperPage.getRecords().forEach(product ->
                productUnderMainCategoryVOS.add(ProductUnderMainCategoryVO.builder()
                        .productId(product.getId())
                        .name(product.getName())
                        .productType(product.getType())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .imageUrl(productImageMapper.selectImagesByProductId(product.getId()).get(0).getImageUrl())
                        .build()));

        return PageVO.<ProductUnderMainCategoryVO>builder()
                .currentPage(productPage.getCurrent())
                .totalPages(productPage.getPages())
                .totalCount(productPage.getTotal())
                .totalRecords(productUnderMainCategoryVOS)
                .build();
    }

    @Override
    public PageVO<ProductUnderSubCategoryVO> getProductsUnderSubCategory(ProductUnderSubCategoryDTO productUnderSubCategoryDTO, CategoryFilterDTO categoryFilterDTO) {
        // 构建分页对象
        Long currentPage = productUnderSubCategoryDTO.getCurrentPage();
        Long pageSize = productUnderSubCategoryDTO.getPageSize();
        Page<ProductSuper> productPage = new Page<>(currentPage, pageSize);

        // 获取筛选条件
        LambdaQueryWrapper<ProductSuper> productSuperLambdaQueryWrapper = buildPageFilterCondition(categoryFilterDTO);
        productSuperLambdaQueryWrapper.eq(ProductSuper::getSubCategoryId, productUnderSubCategoryDTO.getSubCategoryId())
                .ne(ProductSuper::getStatus, Constants.OFF_SHELF)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG);

        // 根据二级分类ID获取到指定的商品
        Page<ProductSuper> productSuperPage = productSuperMapper.selectPage(productPage, productSuperLambdaQueryWrapper);
        List<ProductUnderSubCategoryVO> productUnderSubCategoryVOS = new ArrayList<>();
        productSuperPage.getRecords().forEach(product ->
                productUnderSubCategoryVOS.add(ProductUnderSubCategoryVO.builder()
                        .productId(product.getId())
                        .name(product.getName())
                        .productType(product.getType())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .imageUrl(productImageMapper.selectImagesByProductId(product.getId()).get(0).getImageUrl())
                        .build()));

        return PageVO.<ProductUnderSubCategoryVO>builder()
                .currentPage(productPage.getCurrent())
                .totalPages(productPage.getPages())
                .totalCount(productPage.getTotal())
                .totalRecords(productUnderSubCategoryVOS)
                .build();
    }

    @Override
    public List<RecommendProductVO> getRecommendations(Long userId, Integer defaultTopN) {
        if (userId == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 1. 查询所有有效订单（排除已取消 status=6）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Order::getStatus, 6);
        List<Order> allOrders = orderMapper.selectList(wrapper);

        // 2. 构建 用户-商品 评分矩阵：Map<userId, Map<productId, score>>
        //    score = 该用户购买该商品的总件数（隐式反馈）
        Map<Long, Map<Long, Double>> userProductMatrix = new HashMap<>();
        for (Order order : allOrders) {
            userProductMatrix
                    .computeIfAbsent(order.getUserId(), k -> new HashMap<>())
                    .merge(order.getProductId(),
                            order.getTotalCount() == null ? 1.0 : order.getTotalCount().doubleValue(),
                            Double::sum);
        }

        // 3. 新用户判定：目标用户在矩阵中不存在
        if (!userProductMatrix.containsKey(userId)) {
            return null;
        }

        Map<Long, Double> targetVector = userProductMatrix.get(userId);

        // 4. 计算目标用户与其他所有用户的余弦相似度
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userProductMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;

            double similarity = RecommendUtils.cosineSimilarity(targetVector, entry.getValue());
            if (similarity > 0) {
                similarityMap.put(otherUserId, similarity);
            }
        }

        // 5. 若没有相似用户，返回空列表（已登录老用户但无邻居）
        if (similarityMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 6. 取 Top-K 相似用户
        List<Map.Entry<Long, Double>> topNeighbors = similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(Constants.TOP_K_NEIGHBORS)
                .collect(Collectors.toList());

        // 7. 加权聚合：对目标用户未购买的商品计算推荐分
        Set<Long> purchasedByTarget = targetVector.keySet();
        Map<Long, Double> recommendScores = new HashMap<>();

        for (Map.Entry<Long, Double> neighbor : topNeighbors) {
            Long neighborId = neighbor.getKey();
            double sim = neighbor.getValue();
            Map<Long, Double> neighborVector = userProductMatrix.get(neighborId);

            for (Map.Entry<Long, Double> productEntry : neighborVector.entrySet()) {
                Long productId = productEntry.getKey();
                if (purchasedByTarget.contains(productId)) continue; // 已购买，跳过

                recommendScores.merge(productId, sim * productEntry.getValue(), Double::sum);
            }
        }

        if (recommendScores.isEmpty()) {
            return Collections.emptyList();
        }

        // 8. 按推荐分降序取 TopN 商品ID
        List<Long> topProductIds = recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(defaultTopN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 9. 查询商品详情并组装 VO
        List<RecommendProductVO> result = new ArrayList<>();
        for (Long productId : topProductIds) {
            ProductSuper product = productSuperMapper.selectById(productId);
            // 过滤已下架或删除的商品
            if (product == null || product.getDeleteFlag() == 1 || product.getStatus() == 3) {
                continue;
            }
            List<ProductImage> images = productImageMapper.selectImagesByProductId(productId);
            String imageUrl = (images != null && !images.isEmpty()) ? images.get(0).getImageUrl() : "";

            result.add(RecommendProductVO.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .productType(product.getType())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .imageUrl(imageUrl)
                    .build());
        }

        return result;
    }

    public LambdaQueryWrapper<ProductSuper> buildPageFilterCondition(CategoryFilterDTO categoryFilterDTO) {
        if (categoryFilterDTO == null) {
            return null;
        }

        LambdaQueryWrapper<ProductSuper> productSuperLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Integer sellCount = categoryFilterDTO.getSellCount();
        Integer latest = categoryFilterDTO.getLatest();
        Integer price = categoryFilterDTO.getPrice();
        BigDecimal maxPrice = categoryFilterDTO.getMaxPrice();
        BigDecimal miniPrice = categoryFilterDTO.getMiniPrice();

        if (sellCount != null) {
            List<OrderCountBO> orderCountBOS = new ArrayList<>();
            if (sellCount == 0) {
                // 订单量升序
                orderCountBOS = orderMapper.selectAllOrdersByOrderCountAsc();
            } else if (sellCount == 1) {
                // 订单量降序
                orderCountBOS = orderMapper.selectAllOrdersByOrderCountDesc();
            }
            // 订单表为空
            if (orderCountBOS == null) {
                return null;
            }
            List<Long> productIds = new ArrayList<>();
            // 当前的销量分类会统计到不同类型的商品ID，本系统为了简化不处理该问题
            orderCountBOS.forEach(orderCountBO -> productIds.add(orderCountBO.getProductId()));
            if (!productIds.isEmpty()) {
                String idsStr = productIds.stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(","));
                productSuperLambdaQueryWrapper.last("ORDER BY FIELD(id, " + idsStr + ")");
            }
        }

        if (latest != null) {
            if (latest == 0) {
                return null;
            }
            // 创建时间降序
            productSuperLambdaQueryWrapper.orderByDesc(ProductSuper::getCreateTime);
        }

        if (price != null) {
            if (price == 0) {
                // 价格升序
                productSuperLambdaQueryWrapper.orderByAsc(ProductSuper::getPrice);
            } else if (price == 1) {
                // 价格降序
                productSuperLambdaQueryWrapper.orderByDesc(ProductSuper::getPrice);
            }
        }

        if (maxPrice != null && miniPrice != null) {
            if (miniPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new PetException("最小价格不能小于0");
            }
            productSuperLambdaQueryWrapper.between(ProductSuper::getPrice, miniPrice, maxPrice);
        }

        return productSuperLambdaQueryWrapper;
    }
}
