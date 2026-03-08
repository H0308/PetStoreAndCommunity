package org.epsda.pets.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import org.apache.ibatis.executor.BatchResult;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.SensitiveWordCategoryMapper;
import org.epsda.pets.mapper.SensitiveWordMapper;
import org.epsda.pets.pojo.SensitiveWord;
import org.epsda.pets.pojo.SensitiveWordCategory;
import org.epsda.pets.pojo.bo.ExcelSensitiveWordBO;
import org.epsda.pets.pojo.dto.SensitiveWordAddDTO;
import org.epsda.pets.pojo.dto.SensitiveWordListDTO;
import org.epsda.pets.pojo.dto.SensitiveWordListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveWordListVO;
import org.epsda.pets.service.AdminSensitiveWordService;
import org.epsda.pets.third.Trie;
import org.epsda.pets.third.TrieHolder;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:43
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminSensitiveWordServiceImpl implements AdminSensitiveWordService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    @Autowired
    private SensitiveWordCategoryMapper sensitiveWordCategoryMapper;
    @Autowired
    private TrieHolder trieHolder;

    @Override
    public PageVO<SensitiveWordListVO> list(SensitiveWordListDTO sensitiveWordListDTO,
                                            SensitiveWordListFilterDTO sensitiveWordListFilterDTO) {
        if (sensitiveWordListDTO == null) {
            throw new PetException("敏感词信息错误，获取列表失败");
        }

        SecurityUtil.checkAdmin(sensitiveWordListDTO.getUserId());

        Long currentPage = sensitiveWordListDTO.getCurrentPage();
        Long pageSize = sensitiveWordListDTO.getPageSize();

        Page<SensitiveWord> sensitiveWordPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<SensitiveWord> sensitiveWordLambdaQueryWrapper =
                this.buildSensitiveWordFilterCondition(sensitiveWordListFilterDTO);
        sensitiveWordLambdaQueryWrapper.eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG);

        Page<SensitiveWord> sensitiveWordPages =
                sensitiveWordMapper.selectPage(sensitiveWordPage, sensitiveWordLambdaQueryWrapper);

        List<SensitiveWord> records = sensitiveWordPages.getRecords();
        List<SensitiveWordListVO> sensitiveWordListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> categoryIds = records.stream().map(SensitiveWord::getCategoryId).toList();
            List<SensitiveWordCategory> sensitiveWordCategories = sensitiveWordCategoryMapper
                    .selectList(new LambdaQueryWrapper<SensitiveWordCategory>()
                    .in(SensitiveWordCategory::getId, categoryIds)
                    .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            // 再保存敏感词分类ID和分类的映射关系，用于构建对象时查询
            Map<Long, SensitiveWordCategory> wordIdCategoryMap = new HashMap<>();
            for (SensitiveWordCategory sensitiveWordCategory : sensitiveWordCategories) {
                wordIdCategoryMap.put(sensitiveWordCategory.getId(), sensitiveWordCategory);
            }

            records.forEach(record ->
                    sensitiveWordListVOS.add(this.buildSensitiveWordListVOFromSensitiveWord(record, wordIdCategoryMap)));
        }

        return PageVO.<SensitiveWordListVO>builder()
                .currentPage(sensitiveWordPages.getCurrent())
                .totalPages(sensitiveWordPages.getPages())
                .totalCount(sensitiveWordPages.getTotal())
                .totalRecords(sensitiveWordListVOS)
                .build();
    }

    @Override
    public Boolean add(SensitiveWordAddDTO sensitiveWordAddDTO) {
        if (sensitiveWordAddDTO == null) {
            throw new PetException("敏感词信息错误，新增敏感词错误");
        }

        SecurityUtil.checkAdmin(sensitiveWordAddDTO.getUserId());
        String word = sensitiveWordAddDTO.getWord();
        Long categoryId = sensitiveWordAddDTO.getCategoryId();
        SensitiveWord sensitiveWord = sensitiveWordMapper.selectOne(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, word)
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWord != null) {
            throw new PetException("敏感词已存在，无法新增");
        }

        SensitiveWordCategory sensitiveWordCategory = sensitiveWordCategoryMapper.selectOne(
                new LambdaQueryWrapper<SensitiveWordCategory>().eq(SensitiveWordCategory::getId, categoryId)
                        .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWordCategory == null) {
            throw new PetException("敏感词分类不存在，无法新增");
        }

        SensitiveWord newSensitiveWord = new SensitiveWord();
        newSensitiveWord.setWord(word);
        newSensitiveWord.setCategoryId(categoryId);
        boolean insertRet = sensitiveWordMapper.insert(newSensitiveWord) == 1;
        if (!insertRet) {
            throw new PetException("新增敏感词失败");
        }

        // 插入到Trie中
        trieHolder.trie.insert(word);

        return true;
    }

    @Override
    @SneakyThrows
    public Long addWithCSV(MultipartFile file, Long userId) {
        if (file == null || userId == null) {
            throw new PetException("文件信息错误，导入失败");
        }
        SecurityUtil.checkAdmin(userId);
        List<ExcelSensitiveWordBO> excelSensitiveWordBOS = EasyExcel
                .read(file.getInputStream()).excelType(ExcelTypeEnum.CSV)
                .head(ExcelSensitiveWordBO.class).sheet().headRowNumber(0)
                .doReadSync();
        // 检查分类ID是否有意义
        Set<Long> categoryIds = excelSensitiveWordBOS.stream()
                .map(ExcelSensitiveWordBO::getCategoryId).collect(Collectors.toSet());
        List<SensitiveWordCategory> sensitiveWordCategories = sensitiveWordCategoryMapper.selectList(
                new LambdaQueryWrapper<SensitiveWordCategory>()
                .in(SensitiveWordCategory::getId, categoryIds)
                .eq(SensitiveWordCategory::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (sensitiveWordCategories.size() != categoryIds.size()) {
            throw new PetException("填写了不存在的分类ID，请检查再提交");
        }
        // 构建待插入集合
        List<SensitiveWord> sensitiveWords = new ArrayList<>();
        excelSensitiveWordBOS.forEach(excelSensitiveWordBO -> {
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(excelSensitiveWordBO.getWord());
            sensitiveWord.setCategoryId(excelSensitiveWordBO.getCategoryId());
            sensitiveWords.add(sensitiveWord);
        });

        // 查询已有集合
        List<SensitiveWord> existedSensitiveWords = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        Map<String, SensitiveWord> existedSensitiveWordMap = new HashMap<>();
        for (var existed : existedSensitiveWords) {
            existedSensitiveWordMap.put(existed.getWord(), existed);
        }
        // 去重
        List<SensitiveWord> distinct = sensitiveWords.stream()
                .filter(word -> !existedSensitiveWordMap.containsKey(word.getWord()))
                .toList();

        List<BatchResult> results = sensitiveWordMapper.insert(distinct);

        for (var word : distinct) {
            trieHolder.trie.insert(word.getWord());
        }

        return results.stream()
                .mapToLong(result -> Arrays.stream(result.getUpdateCounts()).sum())
                .sum();
    }

    @Override
    public Boolean delete(Long wordId, Long userId) {
        if (wordId == null || userId == null) {
            throw new PetException("敏感词信息错误，删除失败");
        }

        SecurityUtil.checkAdmin(userId);

        SensitiveWord sensitiveWord = sensitiveWordMapper.selectOne(new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getId, wordId)
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWord == null) {
            throw new PetException("指定敏感词不存在或者已被删除");
        }

        boolean updateRet = sensitiveWordMapper.update(new LambdaUpdateWrapper<SensitiveWord>()
                .eq(SensitiveWord::getId, wordId)
                .set(SensitiveWord::getDeleteFlag, Constants.DELETED_FLAG)) == 1;

        if (!updateRet) {
            throw new PetException("敏感词更新失败");
        }

        // 重建Trie树
        trieHolder.rebuild();

        return true;
    }

    @Override
    public Boolean batchDelete(List<Long> wordIds, Long userId) {
        if (wordIds == null || userId == null) {
            throw new PetException("敏感词信息错误，删除失败");
        }

        SecurityUtil.checkAdmin(userId);

        List<SensitiveWord> sensitiveWords = sensitiveWordMapper.selectList(new LambdaQueryWrapper<SensitiveWord>()
                .in(SensitiveWord::getId, wordIds)
                .eq(SensitiveWord::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (sensitiveWords.size() != wordIds.size()) {
            throw new PetException("部分敏感词不存在，无法删除");
        }

        int update = sensitiveWordMapper.update(new LambdaUpdateWrapper<SensitiveWord>()
                .in(SensitiveWord::getId, wordIds)
                .set(SensitiveWord::getDeleteFlag, Constants.DELETED_FLAG));

        if (update != wordIds.size()) {
            throw new PetException("存在部分敏感词删除失败");
        }

        // 重建Trie树
        trieHolder.rebuild();

        return true;
    }

    public SensitiveWordListVO buildSensitiveWordListVOFromSensitiveWord(SensitiveWord sensitiveWord,
                                                                         Map<Long, SensitiveWordCategory> wordIdCategoryMap) {
        if (sensitiveWord == null) {
            return null;
        }
        return SensitiveWordListVO.builder()
                .sensitiveId(sensitiveWord.getId())
                .word(sensitiveWord.getWord())
                .categoryId(wordIdCategoryMap.get(sensitiveWord.getCategoryId()).getId())
                .categoryName(wordIdCategoryMap.get(sensitiveWord.getCategoryId()).getName())
                .createTime(sensitiveWord.getCreateTime())
                .build();
    }

    public LambdaQueryWrapper<SensitiveWord> buildSensitiveWordFilterCondition(SensitiveWordListFilterDTO sensitiveWordListFilterDTO) {
        if (sensitiveWordListFilterDTO == null) {
            return null;
        }

        Long categoryId = sensitiveWordListFilterDTO.getCategoryId();
        LambdaQueryWrapper<SensitiveWord> sensitiveWordLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            sensitiveWordLambdaQueryWrapper.eq(SensitiveWord::getCategoryId, categoryId);
        }

        String keyword = sensitiveWordListFilterDTO.getKeyword();
        if (StringUtils.hasText(keyword)) {
            sensitiveWordLambdaQueryWrapper.like(SensitiveWord::getWord, keyword);
        }

        return sensitiveWordLambdaQueryWrapper;
    }
}
