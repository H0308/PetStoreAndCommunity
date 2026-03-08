package org.epsda.pets.service.impl;

import cn.hutool.core.lang.hash.Hash;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.sf.jsqlparser.statement.select.Top;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.HotTopicBO;
import org.epsda.pets.pojo.bo.NewUserCountBO;
import org.epsda.pets.pojo.bo.OrderIdWithTotalPriceBO;
import org.epsda.pets.pojo.vo.HotTopicVO;
import org.epsda.pets.pojo.vo.OutOfStockProductBO;
import org.epsda.pets.pojo.vo.TopTotalPriceProductVO;
import org.epsda.pets.service.AdminIndexService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 20:10
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminIndexServiceImpl implements AdminIndexService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TopicPostMapper topicPostMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private ProductCategorySuperMapper productCategorySuperMapper;
    @Autowired
    private ProductCategorySubMapper productCategorySubMapper;

    @Override
    public Long userCount(Long userId) {
        SecurityUtil.checkAdmin(userId);
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(User::getRoleId, Constants.NORMAL_USER_FLAG));
    }

    @Override
    public Long productCount(Long userId) {
        SecurityUtil.checkAdmin(userId);
        return productSuperMapper.selectCount(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
    }

    @Override
    public Long orderCount(Long userId) {
        SecurityUtil.checkAdmin(userId);

        return orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ne(Order::getStatus, Constants.ORDER_TO_PAY)
                .and(wrapper -> wrapper
                        .ne(Order::getStatus, Constants.ORDER_CANCELLED))
                .and(wrapper -> wrapper
                        .ne(Order::getRefundFlag, Constants.ORDER_TO_REFUND)));
    }

    @Override
    public BigDecimal orderPrice(Long userId) {
        SecurityUtil.checkAdmin(userId);

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ne(Order::getStatus, Constants.ORDER_TO_PAY)
                .and(wrapper -> wrapper
                        .ne(Order::getStatus, Constants.ORDER_CANCELLED))
                .and(wrapper -> wrapper
                        .ne(Order::getRefundFlag, Constants.ORDER_TO_REFUND)));

        return orders.stream().map(Order::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<TopTotalPriceProductVO> topTotalPrice(Long userId) {
        SecurityUtil.checkAdmin(userId);

        List<OrderIdWithTotalPriceBO> orderIdWithTotalPriceBOS = orderMapper.selectTop10TotalPrice();

        List<TopTotalPriceProductVO> topTotalPriceProductVOS = new ArrayList<>();
        List<Long> productIds = orderIdWithTotalPriceBOS.stream().map(OrderIdWithTotalPriceBO::getProductId).toList();
        List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                .in(ProductSuper::getId, productIds));
        Map<Long, ProductSuper> productSuperMap = new HashMap<>();
        productSupers.forEach(productSuper -> productSuperMap.put(productSuper.getId(), productSuper));
        orderIdWithTotalPriceBOS.forEach(order -> topTotalPriceProductVOS.add(
                this.buildTopTotalPriceProductVOFromOrderIdWithTotalPriceBO(order, productSuperMap)));
        return topTotalPriceProductVOS;
    }

    @Override
    public List<NewUserCountBO> newUserCount(Long userId) {
        SecurityUtil.checkAdmin(userId);

        return userMapper.selectNewUserCountByCreateTime();
    }

    @Override
    public List<HotTopicVO> hotTopic(Long userId) {
        SecurityUtil.checkAdmin(userId);
        List<HotTopicBO> hotTopicBOS = topicPostMapper.selectTop5HotTopic();
        List<HotTopicVO> hotTopicVOS = new ArrayList<>();
        List<Long> topicIds = hotTopicBOS.stream().map(HotTopicBO::getTopicId).toList();
        List<Topic> topics = topicMapper.selectList(new LambdaQueryWrapper<Topic>()
                .in(Topic::getId, topicIds)
                .eq(Topic::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Map<Long, Topic> topicMap = new HashMap<>();
        topics.forEach(topic -> topicMap.put(topic.getId(), topic));
        hotTopicBOS.forEach(hotTopicBO -> {
            hotTopicVOS.add(HotTopicVO.builder()
                    .name(topicMap.get(hotTopicBO.getTopicId()).getName())
                    .count(hotTopicBO.getCount())
                    .build());
        });

        return hotTopicVOS;
    }

    @Override
    public List<OutOfStockProductBO> outOfStock(Long userId) {
        SecurityUtil.checkAdmin(userId);

        List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getStatus, Constants.SOLD_OUT)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        List<OutOfStockProductBO> outOfStockProductBOS = new ArrayList<>();
        List<Long> mainCategoryIds = productSupers.stream().map(ProductSuper::getMainCategoryId).toList();
        List<Long> subCategoryIds = productSupers.stream().map(ProductSuper::getSubCategoryId).toList();
        Map<Long, ProductCategorySuper> mainCategoryMap = new HashMap<>();
        List<ProductCategorySuper> productCategorySupers = productCategorySuperMapper.selectList(new LambdaQueryWrapper<ProductCategorySuper>()
                .in(ProductCategorySuper::getId, mainCategoryIds)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        productCategorySupers.forEach(productCategorySuper ->
                mainCategoryMap.put(productCategorySuper.getId(), productCategorySuper));
        Map<Long, ProductCategorySub> subCategoryMap = new HashMap<>();
        List<ProductCategorySub> productCategorySubs = productCategorySubMapper.selectList(new LambdaQueryWrapper<ProductCategorySub>()
                .in(ProductCategorySub::getId, subCategoryIds)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        productCategorySubs.forEach(productCategorySub ->
                subCategoryMap.put(productCategorySub.getId(), productCategorySub));
        if (!productSupers.isEmpty()) {
            productSupers.forEach(productSuper -> {
                outOfStockProductBOS.add(OutOfStockProductBO.builder()
                        .productName(productSuper.getName())
                        .mainCategoryName(mainCategoryMap.get(productSuper.getMainCategoryId()).getName())
                        .subCategoryName(subCategoryMap.get(productSuper.getSubCategoryId()).getName())
                        .build());
            });
        }

        return outOfStockProductBOS;
    }

    public TopTotalPriceProductVO buildTopTotalPriceProductVOFromOrderIdWithTotalPriceBO(OrderIdWithTotalPriceBO order,
                                                                                         Map<Long, ProductSuper> productSuperMap) {
        return TopTotalPriceProductVO.builder()
                .name(productSuperMap.get(order.getProductId()).getName())
                .totalPrice(order.getProductTotal())
                .build();
    }
}
