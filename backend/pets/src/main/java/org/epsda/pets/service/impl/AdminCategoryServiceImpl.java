package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.ProductCategorySubMapper;
import org.epsda.pets.mapper.ProductCategorySuperMapper;
import org.epsda.pets.mapper.ProductSuperMapper;
import org.epsda.pets.pojo.ProductCategorySub;
import org.epsda.pets.pojo.ProductCategorySuper;
import org.epsda.pets.pojo.ProductSuper;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CategorySearchListVO;
import org.epsda.pets.pojo.vo.MainCategoryListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SubCategoryListVO;
import org.epsda.pets.service.AdminCategoryService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 15:45
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminCategoryServiceImpl implements AdminCategoryService {

    @Autowired
    private ProductCategorySuperMapper productCategorySuperMapper;
    @Autowired
    private ProductCategorySubMapper productCategorySubMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageVO<MainCategoryListVO> mainCategory(MainCategoryListDTO mainCategoryListDTO) {
        if (mainCategoryListDTO == null) {
            throw new PetException("分类信息错误，获取一级分类失败");
        }

        SecurityUtil.checkAdmin(mainCategoryListDTO.getUserId());
        Long currentPage = mainCategoryListDTO.getCurrentPage();
        Long pageSize = mainCategoryListDTO.getPageSize();

        Page<ProductCategorySuper> productCategorySuperPage = new Page<>(currentPage, pageSize);
        Page<ProductCategorySuper> productCategorySuperPages = productCategorySuperMapper.selectPage(productCategorySuperPage,
                new LambdaQueryWrapper<ProductCategorySuper>().eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                        .orderByAsc(ProductCategorySuper::getType)); // 按照宠物分类在前，用品分类在后的顺序
        List<ProductCategorySuper> records = productCategorySuperPages.getRecords();
        List<MainCategoryListVO> mainCategoryListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            // 获取子分类个数
            List<Long> categorySuperIds = records.stream().map(ProductCategorySuper::getId).toList();
            Map<Long, Long> categorySubCountMap = getCategorySubCountMap(categorySuperIds);
            // 获取商品个数
            Map<Long, Long> productSuperCountMap = getProductCountMap(categorySuperIds, true);

            records.forEach(record ->
                    mainCategoryListVOS.add(this.buildMainCategoryListVOFromProductCategorySuper(record, categorySubCountMap, productSuperCountMap)));
        }

        return PageVO.<MainCategoryListVO>builder()
                .currentPage(productCategorySuperPages.getCurrent())
                .totalPages(productCategorySuperPages.getPages())
                .totalCount(productCategorySuperPages.getTotal())
                .totalRecords(mainCategoryListVOS)
                .build();
    }

    @Override
    public PageVO<SubCategoryListVO> subCategory(SubCategoryListDTO subCategoryListDTO) {
        if (subCategoryListDTO == null) {
            throw new PetException("分类信息错误，获取二级分类失败");
        }
        SecurityUtil.checkAdmin(subCategoryListDTO.getUserId());
        Long currentPage = subCategoryListDTO.getCurrentPage();
        Long pageSize = subCategoryListDTO.getPageSize();
        Long mainCategoryId = subCategoryListDTO.getMainCategoryId();

        Page<ProductCategorySub> productCategorySubPage = new Page<>(currentPage, pageSize);
        Page<ProductCategorySub> productCategorySubPages = productCategorySubMapper.selectPage(productCategorySubPage,
                new LambdaQueryWrapper<ProductCategorySub>()
                        .eq(ProductCategorySub::getMainCategoryId, mainCategoryId)
                        .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<ProductCategorySub> records = productCategorySubPages.getRecords();
        List<SubCategoryListVO> subCategoryListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            // 获取子分类商品个数
            List<Long> subCategoryIds = records.stream().map(ProductCategorySub::getId).toList();
            Map<Long, Long> productCountMap = this.getProductCountMap(subCategoryIds, false);
            records.forEach(record -> subCategoryListVOS.add(
                    this.buildSubCategoryListVOFromProductCategorySub(record, productCountMap)
            ));
        }

        return PageVO.<SubCategoryListVO>builder()
                .currentPage(productCategorySubPages.getCurrent())
                .totalPages(productCategorySubPages.getPages())
                .totalCount(productCategorySubPages.getTotal())
                .totalRecords(subCategoryListVOS)
                .build();
    }

    @Override
    public CategorySearchListVO search(CategorySearchListDTO categorySearchListDTO) {
        if (categorySearchListDTO == null) {
            throw new PetException("搜索信息错误，获取分类失败");
        }

        SecurityUtil.checkAdmin(categorySearchListDTO.getUserId());
        Long currentPage = categorySearchListDTO.getCurrentPage();
        Long pageSize = categorySearchListDTO.getPageSize();
        String keyword = categorySearchListDTO.getKeyword();

        Page<ProductCategorySuper> productCategorySuperPage = new Page<>(currentPage, pageSize);
        Page<ProductCategorySub> productCategorySubPage = new Page<>(currentPage, pageSize);
        // 先查询一级分类表
        Page<ProductCategorySuper> productCategorySuperPages = productCategorySuperMapper.selectPage(productCategorySuperPage, new LambdaQueryWrapper<ProductCategorySuper>()
                .like(ProductCategorySuper::getName, keyword)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        // 再查询二级分类表
        Page<ProductCategorySub> productCategorySubPages = productCategorySubMapper.selectPage(productCategorySubPage, new LambdaQueryWrapper<ProductCategorySub>()
                .like(ProductCategorySub::getName, keyword)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        // 先判断如果二级分类表查询结果不为空，则构建出一级分类结果和二级分类结果
        // 否则如果一级分类表查询结果不为空，则构建出一级分类结果
        List<ProductCategorySub> categorySubList = productCategorySubPages.getRecords();
        List<ProductCategorySuper> categorySuperList = productCategorySuperPages.getRecords();
        List<SubCategoryListVO> subCategoryListVOS = new ArrayList<>();
        Set<MainCategoryListVO> mainCategoryListVOS = new HashSet<>();
        if (!categorySubList.isEmpty()) {
            List<Long> superCategoryIds = categorySubList.stream().map(ProductCategorySub::getMainCategoryId).toList();
            // 获取商品个数
            Map<Long, Long> productSuperCountMap = getProductCountMap(superCategoryIds, true);
            categorySubList.forEach(categorySub ->
                    subCategoryListVOS.add(this.buildSubCategoryListVOFromProductCategorySub(categorySub, productSuperCountMap)));
            // 获取子分类个数
            Map<Long, Long> categorySubCountMap = getCategorySubCountMap(superCategoryIds);
            List<ProductCategorySuper> productCategorySupers = productCategorySuperMapper
                    .selectList(new LambdaQueryWrapper<ProductCategorySuper>()
                    .in(ProductCategorySuper::getId, superCategoryIds)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
            );
            productCategorySupers.forEach(productCategorySuper ->
                    mainCategoryListVOS.add(this.buildMainCategoryListVOFromProductCategorySuper(productCategorySuper,
                            categorySubCountMap, productSuperCountMap)));
        } else if (!categorySuperList.isEmpty()) {
            List<Long> categorySuperIds = categorySuperList.stream().map(ProductCategorySuper::getId).toList();
            // 获取子分类个数
            Map<Long, Long> categorySubCountMap = this.getCategorySubCountMap(categorySuperIds);
            // 获取商品个数
            Map<Long, Long> productCountMap = this.getProductCountMap(categorySuperIds, true);
            categorySuperList.forEach(record ->
                    mainCategoryListVOS.add(this.buildMainCategoryListVOFromProductCategorySuper(record, categorySubCountMap, productCountMap)));
        }

        PageVO<MainCategoryListVO> mainCategoryListVOPageVO = PageVO.<MainCategoryListVO>builder()
                .currentPage(productCategorySuperPages.getCurrent()).totalPages(productCategorySuperPages.getPages())
                .totalCount(productCategorySuperPages.getTotal()).totalRecords(mainCategoryListVOS.stream().toList()).build();

        PageVO<SubCategoryListVO> subCategoryListVOPageVO = PageVO.<SubCategoryListVO>builder()
                .currentPage(productCategorySubPages.getCurrent()).totalPages(productCategorySubPages.getPages())
                .totalCount(productCategorySubPages.getTotal()).totalRecords(subCategoryListVOS).build();

        return CategorySearchListVO.builder()
                .mainCategoryListVOPageVO(mainCategoryListVOPageVO)
                .subCategoryListVOPageVO(subCategoryListVOPageVO)
                .build();
    }

    @Override
    public Boolean addMainCategory(MainCategoryAddDTO mainCategoryAddDTO) {
        if (mainCategoryAddDTO == null) {
            throw new PetException("一级分类信息错误，新增分类失败");
        }

        Long userId = mainCategoryAddDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        String categoryName = mainCategoryAddDTO.getCategoryName();
        Integer type = mainCategoryAddDTO.getType();

        ProductCategorySuper existed = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                .eq(ProductCategorySuper::getName, categoryName)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (existed != null) {
            throw new PetException("已经有同名的分类名称，修改失败");
        }

        ProductCategorySuper productCategorySuper = new ProductCategorySuper();
        productCategorySuper.setName(categoryName);
        productCategorySuper.setType(type);

        boolean insertRet = productCategorySuperMapper.insert(productCategorySuper) == 1;
        if (!insertRet) {
            throw new PetException("一级分类插入失败");
        }
        // 删除缓存
        String mainCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "main";
        stringRedisTemplate.opsForValue().getAndDelete(mainCategoryKey);

        return true;
    }

    @Override
    public Boolean addSubCategory(SubCategoryDTO subCategoryDTO) {
        if (subCategoryDTO == null) {
            throw new PetException("二级分类信息错误，新增分类失败");
        }

        Long userId = subCategoryDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        Long mainCategoryId = subCategoryDTO.getMainCategoryId();
        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                .eq(ProductCategorySuper::getId, mainCategoryId)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (productCategorySuper == null) {
            throw new PetException("一级分类不存在，无法插入二级分类");
        }

        String categoryName = subCategoryDTO.getCategoryName();
        ProductCategorySub existed = productCategorySubMapper.selectOne(new LambdaQueryWrapper<ProductCategorySub>()
                .eq(ProductCategorySub::getName, categoryName)
                .eq(ProductCategorySub::getMainCategoryId, mainCategoryId)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG)); // 只对同一个一级分类下的二级分类进行重复性检验
        if (existed != null) {
            throw new PetException("已经有同名的分类名称，修改失败");
        }
        ProductCategorySub productCategorySub = new ProductCategorySub();
        productCategorySub.setMainCategoryId(mainCategoryId);
        productCategorySub.setName(categoryName);

        boolean insertRet = productCategorySubMapper.insert(productCategorySub) == 1;
        if (!insertRet) {
            throw new PetException("二级分类插入失败");
        }
        // 删除缓存
        String subCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "sub:" + mainCategoryId;
        stringRedisTemplate.opsForValue().getAndDelete(subCategoryKey);

        return true;
    }

    @Transactional
    @Override
    public Boolean changeMainCategory(MainCategoryChangeDTO mainCategoryChangeDTO) {
        if (mainCategoryChangeDTO == null) {
            throw new PetException("分类信息错误，修改分类失败");
        }

        SecurityUtil.checkAdmin(mainCategoryChangeDTO.getUserId());

        Long categoryId = mainCategoryChangeDTO.getCategoryId();
        ProductCategorySuper productCategorySuper = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                .eq(ProductCategorySuper::getId, categoryId)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (productCategorySuper == null) {
            throw new PetException("指定分类不存在，修改分类失败");
        }

        // 修改分类类型必须确保该类型下没有任何商品
        Integer type = mainCategoryChangeDTO.getType();
        String categoryName = mainCategoryChangeDTO.getCategoryName();
        boolean updateRet = true;
        if (type != null) {
            Long productCount = productSuperMapper.selectCount(new LambdaQueryWrapper<ProductSuper>()
                    .eq(ProductSuper::getMainCategoryId, categoryId)
                    .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (productCount > 0) {
                throw new PetException("当前分类下存在商品，无法修改分类类型");
            }

            // 此时可以修改分类类型
            ProductCategorySuper newCategory = new ProductCategorySuper();
            newCategory.setId(categoryId);
            newCategory.setType(type);
            updateRet = productCategorySuperMapper.updateById(newCategory) == 1;
            if (!updateRet) {
                throw new PetException("一级分类类型修改失败");
            }
        }

        if (StringUtils.hasText(categoryName)) {
            ProductCategorySuper existed = productCategorySuperMapper.selectOne(new LambdaQueryWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getName, categoryName)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            if (existed != null) {
                throw new PetException("已经有同名的分类名称，修改失败");
            }

            updateRet = productCategorySuperMapper.update(new LambdaUpdateWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getId, categoryId)
                    .set(ProductCategorySuper::getName, categoryName)) == 1;
            if (!updateRet) {
                throw new PetException("一级分类名称修改失败");
            }
        }

        // 删除缓存
        String mainCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "main";
        stringRedisTemplate.opsForValue().getAndDelete(mainCategoryKey);

        return true;
    }

    @Transactional
    @Override
    public Boolean changeSubCategory(SubCategoryChangeDTO subCategoryChangeDTO) {
        if (subCategoryChangeDTO == null) {
            throw new PetException("分类信息错误，修改分类失败");
        }

        SecurityUtil.checkAdmin(subCategoryChangeDTO.getUserId());

        Long categoryId = subCategoryChangeDTO.getCategoryId();
        ProductCategorySub productCategorySub = productCategorySubMapper.selectOne(new LambdaUpdateWrapper<ProductCategorySub>()
                .eq(ProductCategorySub::getId, categoryId)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (productCategorySub == null) {
            throw new PetException("指定分类不存在，修改分类失败");
        }

        Long mainCategoryId = subCategoryChangeDTO.getMainCategoryId();
        String categoryName = subCategoryChangeDTO.getCategoryName();
        // 如果修改的是一级分类，需要确保修改的一级分类和原先的一级分类属于同一类型
        // 还需要确保该二级分类下没有商品存在
        boolean updateRet = true;
        if (mainCategoryId != null) {
            Long existedMainCategoryId = productCategorySub.getMainCategoryId();
            ProductCategorySuper existedMainCategory = productCategorySuperMapper.selectOne(new LambdaUpdateWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getId, existedMainCategoryId)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (existedMainCategory == null) {
                throw new PetException("原主分类不存在，无法修改二级分类所属的一级分类");
            }

            ProductCategorySuper newMainCategory = productCategorySuperMapper.selectOne(new LambdaUpdateWrapper<ProductCategorySuper>()
                    .eq(ProductCategorySuper::getId, mainCategoryId)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (newMainCategory == null) {
                throw new PetException("新主分类不存在，无法修改二级分类所属的一级分类");
            }

            if (!newMainCategory.getType().equals(existedMainCategory.getType())) {
                throw new PetException("新主分类类型与原主分类类型不一致，修改失败");
            }

            Long productCount = productSuperMapper.selectCount(new LambdaQueryWrapper<ProductSuper>()
                    .eq(ProductSuper::getSubCategoryId, categoryId)
                    .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

            if (productCount > 0) {
                throw new PetException("当前分类下存在商品，无法修改分类类型");
            }

            ProductCategorySub newCategory = new ProductCategorySub();
            newCategory.setId(categoryId);
            newCategory.setMainCategoryId(mainCategoryId);
            updateRet = productCategorySubMapper.updateById(newCategory) == 1;
            if (!updateRet) {
                throw new PetException("二级分类从属分类更新失败");
            }
        }

        if (StringUtils.hasText(categoryName)) {
            ProductCategorySub existed = productCategorySubMapper.selectOne(new LambdaQueryWrapper<ProductCategorySub>()
                    .eq(ProductCategorySub::getName, categoryName)
                    .eq(ProductCategorySub::getMainCategoryId, productCategorySub.getMainCategoryId())
                    .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG)); // 只对同一个一级分类下的二级分类进行重复性检验
            if (existed != null) {
                throw new PetException("已经有同名的分类名称，修改失败");
            }

            updateRet = productCategorySubMapper.update(new LambdaUpdateWrapper<ProductCategorySub>()
                    .eq(ProductCategorySub::getId, categoryId)
                    .set(ProductCategorySub::getName, categoryName)) == 1;
            if (!updateRet) {
                throw new PetException("二级分类名称修改失败");
            }
        }

        // 删除缓存
        String subCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "sub:" +
                productCategorySub.getMainCategoryId();
        stringRedisTemplate.opsForValue().getAndDelete(subCategoryKey);

        return true;
    }

    @Transactional
    @Override
    public Boolean deleteMainCategory(Long mainCategoryId, Long userId) {
        if (mainCategoryId == null || userId == null) {
            throw new PetException("分类信息错误，无法删除一级分类");
        }

        SecurityUtil.checkAdmin(userId);

        // 主分类Id进行校验
        this.validateDeleteMainCategoryPrerequisite(List.of(mainCategoryId));
        // 调用批量删除
        return this.batchDeleteCategoryByIds(List.of(mainCategoryId), true);
    }

    @Override
    public Boolean deleteSubCategory(Long subCategoryId, Long userId) {
        if (subCategoryId == null || userId == null) {
            throw new PetException("分类信息错误，无法删除一级分类");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateDeleteSubCategoryPrerequisite(List.of(subCategoryId));

        return this.batchDeleteCategoryByIds(List.of(subCategoryId), false);
    }

    @Transactional
    @Override
    public Boolean batchDeleteMainCategory(List<Long> mainCategoryIds, Long userId) {
        if (mainCategoryIds == null || userId == null) {
            throw new PetException("分类信息错误，批量删除一级分类错误");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateDeleteMainCategoryPrerequisite(mainCategoryIds);

        return this.batchDeleteCategoryByIds(mainCategoryIds, true);
    }

    @Transactional
    @Override
    public Boolean batchDeleteSubCategory(List<Long> subCategoryIds, Long userId) {
        if (subCategoryIds == null || userId == null) {
            throw new PetException("分类信息错误，批量删除一级分类错误");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateDeleteSubCategoryPrerequisite(subCategoryIds);

        return this.batchDeleteCategoryByIds(subCategoryIds, false);
    }

    private void validateDeleteMainCategoryPrerequisite(List<Long> mainCategoryIds) {
        List<ProductCategorySuper> productCategorySupers = productCategorySuperMapper
                .selectList(new LambdaQueryWrapper<ProductCategorySuper>()
                .in(ProductCategorySuper::getId, mainCategoryIds)
                .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (productCategorySupers.isEmpty() ||
            productCategorySupers.size() != mainCategoryIds.size()) {
            throw new PetException("存在分类不存在或已经被删除");
        }

        // 先判断该分类下是否有商品
        // 注意，如果删除一级分类时一级分类下有商品，那么一定属于某一个二级分类
        // 所以只需要判断一级分类是否有商品就相当于判断二级分类是否有商品
        // 一旦有商品就不允许删除
        Map<Long, Long> productCountMap = this.getProductCountMap(mainCategoryIds, true);
        // 遍历查询是否有商品数量大于0的情况
        boolean isPass = true;
        for (Map.Entry<Long, Long> pair : productCountMap.entrySet()) {
            if (!pair.getValue().equals(0L)) {
                isPass = false;
                break;
            }
        }

        if (!isPass) {
            throw new PetException("部分分类下存在商品，无法删除分类");
        }

    }

    private void validateDeleteSubCategoryPrerequisite(List<Long> subCategoryIds) {
        List<ProductCategorySub> productCategorySubs = productCategorySubMapper
                .selectList(new LambdaQueryWrapper<ProductCategorySub>()
                        .in(ProductCategorySub::getId, subCategoryIds)
                        .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (productCategorySubs.isEmpty() ||
                productCategorySubs.size() != subCategoryIds.size()) {
            throw new PetException("存在分类不存在或已经被删除");
        }

        Map<Long, Long> productCountMap = this.getProductCountMap(subCategoryIds, false);
        // 遍历查询是否有商品数量大于0的情况
        boolean isPass = true;
        for (Map.Entry<Long, Long> pair : productCountMap.entrySet()) {
            if (!pair.getValue().equals(0L)) {
                isPass = false;
                break;
            }
        }

        if (!isPass) {
            throw new PetException("部分分类下存在商品，无法删除分类");
        }
    }

    private Boolean batchDeleteCategoryByIds(List<Long> categoryIds, boolean isSuper) {
        if (isSuper) {
            // 需要获取到所有子分类的ID，再调用批量删除接口
            List<ProductCategorySuper> categorySuperList = productCategorySuperMapper.selectList(new LambdaQueryWrapper<ProductCategorySuper>()
                    .in(ProductCategorySuper::getId, categoryIds)
                    .eq(ProductCategorySuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            List<Long> categorySuperIds = categorySuperList.stream().map(ProductCategorySuper::getId).toList();
            List<ProductCategorySub> productCategorySubs = productCategorySubMapper.selectList(new LambdaQueryWrapper<ProductCategorySub>()
                    .in(ProductCategorySub::getMainCategoryId, categorySuperIds)
                    .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            // 根据主分类获取到的子分类对象，不会重复，不需要去重
            List<Long> categorySubIds = productCategorySubs.stream().map(ProductCategorySub::getId).toList();
            int updateRows = 0;
            if (!categorySubIds.isEmpty()) {
                // 先删除子分类
                updateRows = productCategorySubMapper.update(new LambdaUpdateWrapper<ProductCategorySub>()
                        .in(ProductCategorySub::getId, categorySubIds)
                        .set(ProductCategorySub::getDeleteFlag, Constants.DELETED_FLAG));

                if (updateRows != categorySubIds.size()) {
                    throw new PetException("二级分类删除失败");
                }
            }

            // 需要删除主分类
            updateRows = productCategorySuperMapper.update(new LambdaUpdateWrapper<ProductCategorySuper>()
                    .in(ProductCategorySuper::getId, categoryIds)
                    .set(ProductCategorySuper::getDeleteFlag, Constants.DELETED_FLAG));

            if (updateRows != categoryIds.size()) {
                throw new PetException("一级分类删除失败");
            }

            // 删除主分类缓存
            String mainCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "main";
            stringRedisTemplate.delete(mainCategoryKey);
        } else {
            int updateRows = productCategorySubMapper.update(new LambdaUpdateWrapper<ProductCategorySub>()
                    .in(ProductCategorySub::getId, categoryIds)
                    .set(ProductCategorySub::getDeleteFlag, Constants.DELETED_FLAG));

            if (updateRows != categoryIds.size()) {
                throw new PetException("二级分类删除失败");
            }
        }

        // 删除子分类缓存
        List<String> subCategoryKeys = new ArrayList<>();
        for (Long mainCategoryId : categoryIds) {
            String subCategoryKey = Constants.REDIS_INDEX_CATEGORY_PREFIX + "sub:" + mainCategoryId;
            subCategoryKeys.add(subCategoryKey);
        }
        stringRedisTemplate.delete(subCategoryKeys);

        return true;
    }

    private Map<Long, Long> getProductCountMap(List<Long> categoryIds, boolean isSuper) {
        // 获取商品个数
        List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                .in(isSuper, ProductSuper::getMainCategoryId, categoryIds)
                .in(!isSuper, ProductSuper::getSubCategoryId, categoryIds)
                .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Map<Long, Long> productSuperCountMap = new HashMap<>();
        // 默认没有任何商品
        for (Long categoryId : categoryIds) {
            productSuperCountMap.put(categoryId, 0L);
        }
        for (ProductSuper productSuper : productSupers) {
            productSuperCountMap.compute(isSuper ?
                            productSuper.getMainCategoryId() :
                            productSuper.getSubCategoryId(),
                    (key, productCount) -> productCount + 1);
        }

        return productSuperCountMap;
    }

    private Map<Long, Long> getCategorySubCountMap(List<Long> superCategoryIds) {
        // 获取子分类个数
        List<ProductCategorySub> productCategorySubs = productCategorySubMapper
                .selectList(new LambdaQueryWrapper<ProductCategorySub>()
                .in(ProductCategorySub::getMainCategoryId, superCategoryIds)
                .eq(ProductCategorySub::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Map<Long, Long> categorySubCountMap = new HashMap<>();
        // 先将所有ID初始化为0，防止返回给前端的数据存在null情况
        for (Long categorySuperId : superCategoryIds) {
            categorySubCountMap.put(categorySuperId, 0L);
        }
        for (ProductCategorySub productCategorySub : productCategorySubs) {
            categorySubCountMap.compute(productCategorySub.getMainCategoryId(),
                    (key, subCategoryCount) -> subCategoryCount + 1);
        }

        return categorySubCountMap;
    }

    private MainCategoryListVO buildMainCategoryListVOFromProductCategorySuper(ProductCategorySuper productCategorySuper,
                                                                              Map<Long, Long> subCategoryCountMap,
                                                                              Map<Long, Long> productCountMap) {
        if (productCategorySuper == null) {
            return null;
        }

        Long categorySuperId = productCategorySuper.getId();
        String categorySuperName = productCategorySuper.getName();
        Integer type = productCategorySuper.getType();

        return MainCategoryListVO.builder()
                .categoryId(categorySuperId).categoryName(categorySuperName).type(type)
                .productCount(productCountMap.get(categorySuperId))
                .subCategoryCount(subCategoryCountMap.get(categorySuperId))
                .build();
    }

    private SubCategoryListVO buildSubCategoryListVOFromProductCategorySub(ProductCategorySub productCategorySub,
                                                                            Map<Long, Long> productSuperCountMap) {
        if (productCategorySub == null) {
            return null;
        }

        String name = productCategorySub.getName();
        Long categorySubId = productCategorySub.getId();
        Long mainCategoryId = productCategorySub.getMainCategoryId();
        return SubCategoryListVO.builder()
                .categoryId(categorySubId).mainCategoryId(mainCategoryId)
                .categoryName(name).productCount(productSuperCountMap.get(categorySubId))
                .build();
    }

}
