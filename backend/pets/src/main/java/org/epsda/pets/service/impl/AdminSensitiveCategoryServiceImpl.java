package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.SensitiveWordCategoryMapper;
import org.epsda.pets.mapper.SensitiveWordMapper;
import org.epsda.pets.pojo.SensitiveWord;
import org.epsda.pets.pojo.SensitiveWordCategory;
import org.epsda.pets.pojo.dto.SensitiveCategoryAddDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryChangeDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryListVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryVO;
import org.epsda.pets.pojo.vo.SensitiveWordListVO;
import org.epsda.pets.service.AdminSensitiveCategoryService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 19:14
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminSensitiveCategoryServiceImpl implements AdminSensitiveCategoryService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    @Autowired
    private SensitiveWordCategoryMapper sensitiveWordCategoryMapper;

    @Override
    public PageVO<SensitiveCategoryListVO> list(SensitiveCategoryListDTO sensitiveCategoryListDTO,
                                        SensitiveCategoryListFilterDTO sensitiveCategoryListFilterDTO) {
        if (sensitiveCategoryListDTO == null) {
            throw new PetException("敏感词分类信息错误，获取敏感词分类列表失败");
        }

        SecurityUtil.checkAdmin(sensitiveCategoryListDTO.getUserId());

        Long currentPage = sensitiveCategoryListDTO.getCurrentPage();
        Long pageSize = sensitiveCategoryListDTO.getPageSize();
        Page<SensitiveWordCategory> sensitiveWordCategoryPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<SensitiveWordCategory> sensitiveWordCategoryLambdaQueryWrapper =
                 this.buildSensitiveCategoryFilterCondition(sensitiveCategoryListFilterDTO);
        sensitiveWordCategoryLambdaQueryWrapper.eq(SensitiveWordCategory::getDeleteFlag,
                Constants.NOT_DELETED_FLAG);
        Page<SensitiveWordCategory> sensitiveWordCategoryPages = sensitiveWordCategoryMapper.selectPage(sensitiveWordCategoryPage,
                sensitiveWordCategoryLambdaQueryWrapper);
        List<SensitiveWordCategory> records = sensitiveWordCategoryPages.getRecords();
        List<SensitiveCategoryListVO> sensitiveCategoryListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            records.forEach(record ->
                    sensitiveCategoryListVOS.add(this.buildSensitiveCategoryListVOSFromSensitiveCategory(record)));
        }

        return PageVO.<SensitiveCategoryListVO>builder()
                .currentPage(sensitiveWordCategoryPages.getCurrent())
                .totalPages(sensitiveWordCategoryPages.getPages())
                .totalCount(sensitiveWordCategoryPages.getTotal())
                .totalRecords(sensitiveCategoryListVOS)
                .build();
    }

    @Override
    public List<SensitiveCategoryVO> getAll(Long userId) {
        if (userId == null) {
            throw new PetException("敏感词分类信息错误");
        }

        SecurityUtil.checkAdmin(userId);

        List<SensitiveWordCategory> sensitiveWordCategories = sensitiveWordCategoryMapper.selectList(new LambdaQueryWrapper<SensitiveWordCategory>()
                .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (sensitiveWordCategories.isEmpty()) {
            return null;
        }

        List<SensitiveCategoryVO> sensitiveCategoryVOS = new ArrayList<>();
        sensitiveWordCategories.forEach(category -> {
            SensitiveCategoryVO sensitiveCategoryVO = SensitiveCategoryVO.builder()
                    .categoryName(category.getName()).categoryId(category.getId())
                    .build();

            sensitiveCategoryVOS.add(sensitiveCategoryVO);
        });

        return sensitiveCategoryVOS;
    }

    @Override
    public Boolean add(SensitiveCategoryAddDTO sensitiveCategoryAddDTO) {
        if (sensitiveCategoryAddDTO == null) {
            throw new PetException("敏感词分类信息错误，新增失败");
        }

        SecurityUtil.checkAdmin(sensitiveCategoryAddDTO.getUserId());

        String name = sensitiveCategoryAddDTO.getName();
        SensitiveWordCategory existed = sensitiveWordCategoryMapper.selectOne(
                new LambdaQueryWrapper<SensitiveWordCategory>()
                .eq(SensitiveWordCategory::getName, name)
                .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (existed != null) {
            throw new PetException("当前分类已经存在");
        }

        SensitiveWordCategory sensitiveWordCategory = new SensitiveWordCategory();
        sensitiveWordCategory.setName(name);
        boolean insertRet = sensitiveWordCategoryMapper.insert(sensitiveWordCategory) == 1;
        if (!insertRet) {
            throw new PetException("新增分类失败");
        }

        return true;
    }

    @Override
    public Boolean change(SensitiveCategoryChangeDTO sensitiveCategoryChangeDTO) {
        if (sensitiveCategoryChangeDTO == null) {
            throw new PetException("敏感词信息错误，修改失败");
        }

        SecurityUtil.checkAdmin(sensitiveCategoryChangeDTO.getUserId());

        Long categoryId = sensitiveCategoryChangeDTO.getCategoryId();
        SensitiveWordCategory sensitiveWordCategory = sensitiveWordCategoryMapper.selectOne(
                new LambdaQueryWrapper<SensitiveWordCategory>()
                .eq(SensitiveWordCategory::getId, categoryId)
                .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWordCategory == null) {
            throw new PetException("指定分类不存在，修改失败");
        }

        boolean updateRet = sensitiveWordCategoryMapper.update(
                new LambdaUpdateWrapper<SensitiveWordCategory>()
                        .eq(SensitiveWordCategory::getId, categoryId)
                        .set(SensitiveWordCategory::getName, sensitiveCategoryChangeDTO.getName())) == 1;

        if (!updateRet) {
            throw new PetException("修改失败");
        }

        return true;
    }

    @Override
    public Boolean delete(Long categoryId, Long userId) {
        if (categoryId == null || userId == null) {
            throw new PetException("敏感词分类信息错误，删除失败");
        }

        SecurityUtil.checkAdmin(userId);

        SensitiveWordCategory sensitiveWordCategory = sensitiveWordCategoryMapper.selectOne(
                new LambdaQueryWrapper<SensitiveWordCategory>()
                        .eq(SensitiveWordCategory::getId, categoryId)
                        .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWordCategory == null) {
            throw new PetException("指定分类不存在，删除失败");
        }

        Long wordCount = sensitiveWordMapper.selectCount(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getCategoryId, categoryId)
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (wordCount > 0) {
            throw new PetException("当前分类下存在敏感词，无法删除");
        }

        boolean updateRet = sensitiveWordCategoryMapper.update(
                new LambdaUpdateWrapper<SensitiveWordCategory>()
                        .eq(SensitiveWordCategory::getId, categoryId)
                        .set(SensitiveWordCategory::getDeleteFlag, Constants.DELETED_FLAG)) == 1;

        if (!updateRet) {
            throw new PetException("修改失败");
        }

        return true;
    }

    @Override
    public Boolean batchDelete(List<Long> categoryIds, Long userId) {
        if (categoryIds == null || userId == null) {
            throw new PetException("敏感词分类信息错误，删除失败");
        }

        SecurityUtil.checkAdmin(userId);
        for (var categoryId : categoryIds) {
            Boolean delete = this.delete(categoryId, userId);
            if (!delete) {
                throw new PetException("存在分类删除失败，终止删除");
            }
        }

        return true;
    }

    public SensitiveCategoryListVO buildSensitiveCategoryListVOSFromSensitiveCategory(SensitiveWordCategory sensitiveWordCategory) {
        if (sensitiveWordCategory == null) {
            return null;
        }

        Long wordCount = sensitiveWordMapper.selectCount(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getCategoryId, sensitiveWordCategory.getId())
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        return SensitiveCategoryListVO.builder()
                .categoryId(sensitiveWordCategory.getId())
                .categoryName(sensitiveWordCategory.getName())
                .wordCount(wordCount)
                .createTime(sensitiveWordCategory.getCreateTime())
                .build();
    }

    public LambdaQueryWrapper<SensitiveWordCategory> buildSensitiveCategoryFilterCondition(SensitiveCategoryListFilterDTO sensitiveCategoryListFilterDTO) {
        if (sensitiveCategoryListFilterDTO == null) {
            return null;
        }

        String keyword = sensitiveCategoryListFilterDTO.getKeyword();
        LambdaQueryWrapper<SensitiveWordCategory> sensitiveWordCategoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            sensitiveWordCategoryLambdaQueryWrapper.like(SensitiveWordCategory::getName, keyword);
        }

        return sensitiveWordCategoryLambdaQueryWrapper;
    }
}
