package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.schema.Column;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.ColumnInfoMapper;
import org.epsda.pets.mapper.PostMapper;
import org.epsda.pets.pojo.ColumnInfo;
import org.epsda.pets.pojo.Post;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.ColumnListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.AdminColumnService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 17:31
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminColumnServiceImpl implements AdminColumnService {

    @Autowired
    private ColumnInfoMapper columnInfoMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageVO<ColumnListVO> list(ColumnListDTO columnListDTO, ColumnListFilterDTO columnListFilterDTO) {
        if (columnListDTO == null) {
            throw new PetException("栏目信息错误，获取栏目列表失败");
        }

        SecurityUtil.checkAdmin(columnListDTO.getUserId());

        Long currentPage = columnListDTO.getCurrentPage();
        Long pageSize = columnListDTO.getPageSize();
        Page<ColumnInfo> columnInfoPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<ColumnInfo> columnInfoLambdaQueryWrapper =
                this.buildColumnFilterCondition(columnListFilterDTO);
        columnInfoLambdaQueryWrapper.eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        Page<ColumnInfo> topicPages = columnInfoMapper.selectPage(columnInfoPage, columnInfoLambdaQueryWrapper);
        List<ColumnInfo> records = topicPages.getRecords();
        List<ColumnListVO> columnListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> columnIds = records.stream().map(ColumnInfo::getId).toList();
            Map<Long, Long> topicIdPostCountMap = this.getPostCountMap(columnIds);
            records.forEach(record -> columnListVOS.add(this.buildColumnListVOFromTopic(record, topicIdPostCountMap)));
        }

        // 根据帖子数量进行排序
        Integer sortFlag = columnListFilterDTO.getSortFlag();
        if (sortFlag.equals(Constants.DESC_FLAG)) {
            // 调用toIntExact将long转换为int，可以防止溢出问题（溢出抛异常）
            columnListVOS.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    throw new PetException("存在空数据");
                }
                return Math.toIntExact(o2.getPostCount() - o1.getPostCount());
            });
        } else if (sortFlag.equals(Constants.ASC_FLAG)) {
            columnListVOS.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    throw new PetException("存在空数据");
                }
                return Math.toIntExact(o1.getPostCount() - o2.getPostCount());
            });
        }

        return PageVO.<ColumnListVO>builder()
                .currentPage(topicPages.getCurrent())
                .totalPages(topicPages.getPages())
                .totalCount(topicPages.getTotal())
                .totalRecords(columnListVOS)
                .build();
    }

    @Override
    public Boolean change(ColumnChangeDTO columnChangeDTO) {
        if (columnChangeDTO == null) {
            throw new PetException("栏目信息错误，无法修改栏目");
        }

        SecurityUtil.checkAdmin(columnChangeDTO.getUserId());
        ColumnInfo columnInfo = columnInfoMapper.selectOne(new LambdaQueryWrapper<ColumnInfo>()
                .eq(ColumnInfo::getId, columnChangeDTO.getColumnId())
                .eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (columnInfo == null) {
            throw new PetException("栏目不存在，无法修改");
        }

        String columnName = columnChangeDTO.getColumnName();
        if (StringUtils.hasText(columnName)) {
            boolean updateRet = columnInfoMapper.update(new LambdaUpdateWrapper<ColumnInfo>()
                    .eq(ColumnInfo::getId, columnChangeDTO.getColumnId())
                    .set(ColumnInfo::getName, columnName)) == 1;

            if (!updateRet) {
                throw new PetException("栏目更新失败");
            }
        }

        // 删除缓存
        stringRedisTemplate.delete(Constants.REDIS_POST_COLUMN);

        return true;
    }

    @Override
    public Boolean add(ColumnAddDTO columnAddDTO) {
        if (columnAddDTO == null) {
            throw new PetException("栏目信息错误，新增栏目失败");
        }

        SecurityUtil.checkAdmin(columnAddDTO.getUserId());

        String topicName = columnAddDTO.getColumnName();
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setName(topicName);
        boolean insertRet = columnInfoMapper.insert(columnInfo) == 1;
        if (!insertRet) {
            throw new PetException("栏目新增失败");
        }

        // 删除缓存
        stringRedisTemplate.delete(Constants.REDIS_POST_COLUMN);

        return true;
    }

    @Override
    public Boolean delete(Long columnId, Long userId) {
        if (columnId == null || userId == null) {
            throw new PetException("栏目信息错误，新增栏目失败");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateBatchDeletePrerequisite(List.of(columnId));

        return this.batchDeleteColumnByIds(List.of(columnId));
    }

    @Override
    public Boolean batchDelete(List<Long> columnIds, Long userId) {
        if (columnIds == null || columnIds.isEmpty() || userId == null) {
            throw new PetException("栏目信息错误，删除栏目失败");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateBatchDeletePrerequisite(columnIds);

        return this.batchDeleteColumnByIds(columnIds);
    }

    private boolean batchDeleteColumnByIds(List<Long> columnIds) {
        int updateRows = columnInfoMapper.update(new LambdaUpdateWrapper<ColumnInfo>()
                .in(ColumnInfo::getId, columnIds)
                .set(ColumnInfo::getDeleteFlag, Constants.DELETED_FLAG));

        if (updateRows != columnIds.size()) {
            throw new PetException("栏目更新失败");
        }

        // 删除缓存
        stringRedisTemplate.delete(Constants.REDIS_POST_COLUMN);

        return true;
    }

    private void validateBatchDeletePrerequisite(List<Long> columnIds) {
        List<ColumnInfo> columnInfos = columnInfoMapper.selectList(new LambdaQueryWrapper<ColumnInfo>()
                .in(ColumnInfo::getId, columnIds)
                .eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (columnInfos.isEmpty()
            || columnInfos.size() != columnIds.size()) {
            throw new PetException("存在栏目不存在或者已经被删除，无法继续删除");
        }

        Map<Long, Long> postCountMap = this.getPostCountMap(columnIds);
        boolean isZero = true;
        for (Map.Entry<Long, Long> pair : postCountMap.entrySet()) {
            if (!pair.getValue().equals(0L)) {
                isZero = false;
                break;
            }
        }
        if (!isZero) {
            throw new PetException("部分栏目下存在帖子，无法删除");
        }
    }

    private Map<Long, Long> getPostCountMap(List<Long> columnIds) {
        // 记录栏目ID和帖子个数的映射Map
        Map<Long, Long> columnIdPostCountMap = new HashMap<>();
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getColumnId, columnIds)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        for (Long columnId : columnIds) {
            columnIdPostCountMap.put(columnId, 0L);
        }
        for (Post post : posts) {
            columnIdPostCountMap.compute(post.getColumnId(), (key, postCount) -> postCount + 1);
        }
        return columnIdPostCountMap;
    }

    private ColumnListVO buildColumnListVOFromTopic(ColumnInfo columnInfo, Map<Long, Long> topicIdPostCountMap) {
        if (columnInfo == null) {
            return null;
        }

        Long columnInfoId = columnInfo.getId();
        String topicName = columnInfo.getName();

        return ColumnListVO.builder()
                .columnId(columnInfoId).columnName(topicName)
                .postCount(topicIdPostCountMap.get(columnInfoId))
                .build();
    }

    private LambdaQueryWrapper<ColumnInfo> buildColumnFilterCondition(ColumnListFilterDTO columnListFilterDTO) {
        if (columnListFilterDTO == null) {
            return null;
        }

        String columnName = columnListFilterDTO.getColumnName();
        LambdaQueryWrapper<ColumnInfo> columnInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(columnName)) {
            columnInfoLambdaQueryWrapper.like(ColumnInfo::getName, columnName);
        }

        return columnInfoLambdaQueryWrapper;
    }
}
