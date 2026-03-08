package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.CartMapper;
import org.epsda.pets.mapper.ProductImageMapper;
import org.epsda.pets.mapper.ProductSuperMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Cart;
import org.epsda.pets.pojo.ProductImage;
import org.epsda.pets.pojo.ProductSuper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.document.CartDocument;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.repository.CartRepository;
import org.epsda.pets.service.CartService;
import org.epsda.pets.service.OrderService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/16
 * Time: 12:01
 *
 * @Author: 憨八嘎
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductImageMapper productImageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void buildDataIntoES() {
        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 选择没有删除的商品
        cartLambdaQueryWrapper.eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        List<Cart> carts = cartMapper.selectList(cartLambdaQueryWrapper);
        List<CartDocument> cartDocuments = new ArrayList<>();
        carts.forEach(cart -> cartDocuments.add(this.buildCartDocumentFromCart(cart)));

        cartRepository.saveAll(cartDocuments);
    }

    @Override
    public Long getCounts(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户信息错误，获取购物车商品数量失败");
        }

        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，获取购物车商品数量失败");
        }

        LambdaQueryWrapper<Cart> cartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        cartLambdaQueryWrapper.eq(Cart::getUserId, userId).eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        return cartMapper.selectCount(cartLambdaQueryWrapper);
    }

    @Override
    public CartChangeVO change(CartChangeDTO cartChangeDTO) {
        if (cartChangeDTO == null || cartChangeDTO.getCartId() == null ||
            cartChangeDTO.getCartId() == 0) {
            throw new PetException("商品信息错误，修改失败");
        }

        Long cartId = cartChangeDTO.getCartId();
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (cart == null) {
            throw new PetException("商品不存在于购物车中，修改失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(cart.getUserId());
        boolean updateRet = false;
        Long totalCount = cartChangeDTO.getTotalCount();
        BigDecimal totalPrice = null;
        Long stock = null;
        if (totalCount != null) {
            ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                    .eq(ProductSuper::getId, cart.getProductId())
                    .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                    .and(productSuperQueryWrapper -> productSuperQueryWrapper
                            .eq(ProductSuper::getStatus, Constants.ON_SELLING)
                            .or()
                            .eq(ProductSuper::getStatus, Constants.SOLD_OUT)));
            if (totalCount <= 0) {
                throw new PetException("商品最小数量为1");
            }
            if (productSuper == null) {
                throw new PetException("商品已下架或者不存在，修改失败");
            }
            stock = productSuper.getStock();
            if (stock.compareTo(totalCount)< 0 || productSuper.getStatus().equals(Constants.OFF_SHELF)) {
                throw new PetException("当前库存量不足或者商品已下架，无法购买当前商品");
            }

            Cart newCart = new Cart();
            newCart.setId(cartId);
            newCart.setTotalCount(totalCount);
            totalPrice = productSuper.getPrice().multiply(new BigDecimal(totalCount));
            newCart.setTotalPrice(totalPrice);

            updateRet = cartMapper.updateById(newCart) == 1;
        }

        if (!updateRet) {
            throw new PetException("修改失败");
        }

        return CartChangeVO.builder()
                .totalCount(totalCount).totalPrice(totalPrice)
                .build();
    }

    @Override
    public PreOrderDetailVO settle(Long cartId) {
        if (cartId == null || cartId == 0) {
            throw new PetException("购物车商品信息有误，无法结算");
        }

        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (cart == null) {
            throw new PetException("购物车商品ID错误，无法结算");
        }

        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(cart.getUserId());

        return createOrder(cart);
    }

    @Override
    public Boolean delete(Long cartId) {
        if (cartId == null || cartId == 0) {
            throw new PetException("购物车商品ID错误，删除失败");
        }

        Cart existed = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, cartId)
                .eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (existed == null) {
            throw new PetException("购物车商品不存在，无法删除");
        }

        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(existed.getUserId());

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setDeleteFlag(Constants.DELETED_FLAG);
        boolean updateRet = cartMapper.updateById(cart) == 1;
        if (!updateRet) {
            throw new PetException("购物车移除商品失败");
        }
        CartDocument cartDocument = cartRepository.findCartDocumentByCartId(cartId);
        if (cartDocument != null) {
            cartRepository.delete(cartDocument);
        }

        return true;
    }

    @Override
    public List<Boolean> batchDelete(List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw new PetException("购物车商品信息错误，批量删除失败");
        }

        return cartIds.stream().map(this::delete).toList();
    }

    @Override
    public PageVO<CartProductDetailVO> list(CartDetailDTO cartDetailDTO) {
        if (cartDetailDTO == null) {
            throw new PetException("查询信息错误，查询失败");
        }

        Long currentPage = cartDetailDTO.getCurrentPage();
        Long pageSize = cartDetailDTO.getPageSize();
        Long userId = cartDetailDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，获取购物车列表失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 构建查询和分页
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(Math.toIntExact(currentPage - 1), Math.toIntExact(pageSize), sort);
        String keyword = cartDetailDTO.getKeyword();

        Page<CartDocument> cartDocumentPage;
        if (!StringUtils.hasText(keyword)) {
            // 直接获取所有数据
            cartDocumentPage = cartRepository.findCartDocumentsByUserId(userId, pageable);
        } else {
            String fieldName = (keyword.length() <= 1)
                    ? "productName.ngram"
                    : "productName";
            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                    .bool(bool -> bool
                       .must(must -> must
                            .match(match -> match
                                .field(fieldName)
                                .query(keyword)))
                            .filter(filter -> filter
                                .term(term -> term
                                    .field("userId")
                                    .value(userId)))))
                    .withPageable(pageable)
                    .build();
            // 进行查询
            SearchHits<CartDocument> search = elasticsearchOperations.search(nativeQuery, CartDocument.class);
            cartDocumentPage = SearchHitSupport.searchPageFor(search, pageable).map(SearchHit::getContent);
        }

        List<CartDocument> content = cartDocumentPage.getContent();
        List<CartProductDetailVO> cartProductDetailVOS = new ArrayList<>();
        content.forEach(cartDocument ->
                cartProductDetailVOS.add(this.buildCartProductDetailVOFromCartDocument(cartDocument)));

        return PageVO.<CartProductDetailVO>builder()
                .currentPage((long) (cartDocumentPage.getNumber() + 1))
                .totalPages((long) cartDocumentPage.getTotalPages())
                .totalCount(cartDocumentPage.getTotalElements())
                .totalRecords(cartProductDetailVOS)
                .build();
    }

    @Override
    public List<PreOrderDetailVO> batchSettle(List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw new PetException("购物车商品为空，无法结算");
        }

        // 错误的订单将被回滚
        return cartIds.stream().map(this::settle).toList();
    }

    @Override
    public Boolean add(AddToCartDTO addToCartDTO) {
        if (addToCartDTO == null) {
            throw new PetException("插入的数据无效，无法添加到购物车");
        }

        Long userId = addToCartDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，添加购物车失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long productId = addToCartDTO.getProductId();
        ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                .eq(ProductSuper::getId, productId)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .and(productSuperQueryWrapper -> productSuperQueryWrapper
                        .eq(ProductSuper::getStatus, Constants.ON_SELLING)
                        .or()
                        .eq(ProductSuper::getStatus, Constants.SOLD_OUT)));
        if (productSuper == null) {
            throw new PetException("商品不存在或者已下架，添加购物车失败");
        }

        Long totalCount = addToCartDTO.getTotalCount();
        Long stock = productSuper.getStock();

        // 插入购物车数据
        Cart cart = new Cart();
        BigDecimal price = productSuper.getPrice();
        // 如果已经有同样的商品，直接更新数量
        Cart existedCart = cartMapper.selectByProductId(productId, userId);
        if (existedCart != null) {
            cart.setId(existedCart.getId());
            Long existedCount = existedCart.getTotalCount();
            totalCount += existedCount;
            if (stock.compareTo(totalCount) < 0){
                throw new PetException("当前库存量不足或者商品已下架，无法购买当前商品");
            }
            cart.setTotalCount(totalCount);
            cart.setTotalPrice(price.multiply(new BigDecimal(totalCount)));
            boolean updateRet = cartMapper.updateById(cart) == 1;
            if (!updateRet) {
                throw new PetException("购物车商品数量更新失败");
            }

            return true;
        }

        // 如果没有再插入新数据
        if (stock.compareTo(totalCount)< 0) {
            throw new PetException("当前库存量不足或者商品已下架，无法购买当前商品");
        }
        CartDocument cartDocument = new CartDocument();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setTotalCount(totalCount);
        cart.setTotalPrice(price.multiply(new BigDecimal(totalCount)));
        boolean insertRet = cartMapper.insert(cart) == 1;
        if (!insertRet) {
            throw new PetException("购物车新增商品失败");
        }

        cartDocument.setCartId(cart.getId());
        cartDocument.setUserId(userId);
        cartDocument.setProductId(productId);
        cartDocument.setProductName(productSuper.getName());
        cartDocument.setProductType(productSuper.getType());
        cartDocument.setProductStatus(productSuper.getStatus());
        List<ProductImage> productImages = productImageMapper.selectImagesByProductId(productId);
        cartDocument.setProductImageUrl(productImages.get(0).getImageUrl());
        cartDocument.setCreateTime(cart.getCreateTime());
        cartRepository.save(cartDocument);

        return true;
    }

    public PreOrderDetailVO createOrder(Cart cart) {
        OrderPreCreateDTO orderPreCreateDTO = new OrderPreCreateDTO();
        orderPreCreateDTO.setUserId(cart.getUserId());
        orderPreCreateDTO.setProductId(cart.getProductId());
        orderPreCreateDTO.setTotalCount(cart.getTotalCount());
        PreOrderDetailVO preOrderDetailVO = null;
        try {
            preOrderDetailVO = orderService.preCreate(orderPreCreateDTO);
        } catch (PetException petException) {
            String msg = petException.getMessage();
            // 实名认证、手机号、收货地址校验失败需要向上抛出，让前端展示对应提示
            if (msg != null && (msg.contains("实名认证") || msg.contains("手机号") || msg.contains("收货地址"))) {
                throw petException;
            }
            return null;
        }

        return preOrderDetailVO;
    }

    public CartDocument buildCartDocumentFromCart(Cart cart) {
        if (cart == null) {
            return null;
        }

        Long userId = cart.getUserId();
        Long productId = cart.getProductId();

        ProductSuper productSuper = productSuperMapper.selectById(productId);
        String productName = productSuper.getName();
        Integer productType = productSuper.getType();
        Integer productStatus = productSuper.getStatus();

        return CartDocument.builder().userId(userId).cartId(cart.getId()).productId(productId)
                .productType(productType).productName(productName)
                .productImageUrl(productImageMapper.selectImagesByProductId(productId).get(0).getImageUrl())
                .productStatus(productStatus)
                .createTime(cart.getCreateTime())
                .build();
    }

    public CartProductDetailVO buildCartProductDetailVOFromCartDocument(CartDocument cartDocument) {
        if (cartDocument == null) {
            return null;
        }

        Long productId = cartDocument.getProductId();
        Long userId = cartDocument.getUserId();
        Cart cart = cartMapper.selectByProductId(productId, userId);
        ProductSuper productSuper = productSuperMapper.selectById(productId);
        String productName = productSuper.getName();
        Integer productType = productSuper.getType();
        Integer productStatus = productSuper.getStatus();
        BigDecimal price = productSuper.getPrice();

        return CartProductDetailVO.builder().cartId(cartDocument.getCartId()).productId(productId)
                .productType(productType).productName(productName)
                .productImageUrl(productImageMapper.selectImagesByProductId(productId).get(0).getImageUrl())
                .productStatus(productStatus).price(price)
                .totalCount(cart.getTotalCount()).totalPrice(cart.getTotalPrice())
                .createTime(cartDocument.getCreateTime())
                .build();
    }
}
