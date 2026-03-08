package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.TopicMapper;
import org.epsda.pets.mapper.TopicPostMapper;
import org.epsda.pets.pojo.ColumnInfo;
import org.epsda.pets.pojo.Post;
import org.epsda.pets.pojo.Topic;
import org.epsda.pets.pojo.TopicPost;
import org.epsda.pets.pojo.dto.TopicAddDTO;
import org.epsda.pets.pojo.dto.TopicChangeDTO;
import org.epsda.pets.pojo.dto.TopicListDTO;
import org.epsda.pets.pojo.dto.TopicListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.ProductListVO;
import org.epsda.pets.pojo.vo.TopicListVO;
import org.epsda.pets.service.AdminTopicService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Time: 10:04
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminTopicServiceImpl implements AdminTopicService {

    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private TopicPostMapper topicPostMapper;

    @Override
    public PageVO<TopicListVO> list(TopicListDTO topicListDTO, TopicListFilterDTO topicListFilterDTO) {
        if (topicListDTO == null) {
            throw new PetException("话题信息错误，获取话题列表失败");
        }

        SecurityUtil.checkAdmin(topicListDTO.getUserId());

        Long currentPage = topicListDTO.getCurrentPage();
        Long pageSize = topicListDTO.getPageSize();
        Page<Topic> topicPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Topic> topicLambdaQueryWrapper =
                this.buildTopicFilterCondition(topicListFilterDTO);
        topicLambdaQueryWrapper.eq(Topic::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        Page<Topic> topicPages = topicMapper.selectPage(topicPage, topicLambdaQueryWrapper);
        List<Topic> records = topicPages.getRecords();
        List<TopicListVO> topicListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> topicIds = records.stream().map(Topic::getId).toList();
            Map<Long, Long> postCountMap = this.getPostCount(topicIds);
            records.forEach(record -> topicListVOS.add(this.buildTopicListVOFromTopic(record, postCountMap)));
        }

        // 根据帖子数量进行排序
        Integer sortFlag = topicListFilterDTO.getSortFlag();
        if (sortFlag.equals(Constants.DESC_FLAG)) {
            // 调用toIntExact将long转换为int，可以防止溢出问题（溢出抛异常）
            topicListVOS.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    throw new PetException("存在空数据");
                }
                return Math.toIntExact(o2.getPostCount() - o1.getPostCount());
            });
        } else if (sortFlag.equals(Constants.ASC_FLAG)) {
            topicListVOS.sort((o1, o2) -> {
                if (o1 == null || o2 == null) {
                    throw new PetException("存在空数据");
                }
                return Math.toIntExact(o1.getPostCount() - o2.getPostCount());
            });
        }

        return PageVO.<TopicListVO>builder()
                .currentPage(topicPages.getCurrent())
                .totalPages(topicPages.getPages())
                .totalCount(topicPages.getTotal())
                .totalRecords(topicListVOS)
                .build();
    }

    private Map<Long, Long> getPostCount(List<Long> topicIds) {
        // 记录话题ID和帖子个数的映射Map
        Map<Long, Long> topicIdPostCountMap = new HashMap<>();
        List<TopicPost> topicPosts = topicPostMapper.selectList(new LambdaQueryWrapper<TopicPost>()
                .in(TopicPost::getTopicId, topicIds)
                .eq(TopicPost::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        for (Long topicId : topicIds) {
            topicIdPostCountMap.put(topicId, 0L);
        }
        for (TopicPost topicPost : topicPosts) {
            topicIdPostCountMap.compute(topicPost.getTopicId(), (key, postCount) -> postCount + 1);
        }

        return topicIdPostCountMap;
    }

    @Override
    public Boolean change(TopicChangeDTO topicChangeDTO) {
        if (topicChangeDTO == null) {
            throw new PetException("话题信息错误，无法修改话题");
        }

        SecurityUtil.checkAdmin(topicChangeDTO.getUserId());
        Topic topic = topicMapper.selectOne(new LambdaQueryWrapper<Topic>()
                .eq(Topic::getId, topicChangeDTO.getTopicId())
                .eq(Topic::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (topic == null) {
            throw new PetException("话题不存在，无法修改");
        }

        String topicName = topicChangeDTO.getTopicName();
        if (StringUtils.hasText(topicName)) {
            boolean updateRet = topicMapper.update(new LambdaUpdateWrapper<Topic>()
                    .eq(Topic::getId, topicChangeDTO.getTopicId())
                    .set(Topic::getName, topicName)) == 1;

            if (!updateRet) {
                throw new PetException("话题更新失败");
            }
        }

        return true;
    }

    @Override
    public Boolean add(TopicAddDTO topicAddDTO) {
        if (topicAddDTO == null) {
            throw new PetException("话题信息错误，新增话题失败");
        }

        SecurityUtil.checkAdmin(topicAddDTO.getUserId());

        String topicName = topicAddDTO.getTopicName();
        Topic topic = new Topic();
        topic.setName(topicName);
        boolean insertRet = topicMapper.insert(topic) == 1;
        if (!insertRet) {
            throw new PetException("话题新增失败");
        }

        return true;
    }

    @Override
    public Boolean delete(Long topicId, Long userId) {
        if (topicId == null || userId == null) {
            throw new PetException("话题信息错误，新增话题失败");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateBatchDeletePrerequisite(List.of(topicId));

        return this.batchDeleteTopicByIds(List.of(topicId));
    }

    @Override
    public Boolean batchDelete(List<Long> topicIds, Long userId) {
        if (topicIds == null || userId == null) {
            throw new PetException("话题信息错误，删除话题失败");
        }

        SecurityUtil.checkAdmin(userId);

        this.validateBatchDeletePrerequisite(topicIds);

        return this.batchDeleteTopicByIds(topicIds);
    }

    private boolean batchDeleteTopicByIds(List<Long> topicIds) {
        int updateRows = topicMapper.update(new LambdaUpdateWrapper<Topic>()
                .in(Topic::getId, topicIds)
                .set(Topic::getDeleteFlag, Constants.DELETED_FLAG));

        if (updateRows != topicIds.size()) {
            throw new PetException("话题更新失败");
        }

        return true;
    }

    private void validateBatchDeletePrerequisite(List<Long> topicIds) {
        List<Topic> topics = topicMapper.selectList(new LambdaQueryWrapper<Topic>()
                .in(Topic::getId, topicIds)
                .eq(Topic::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (topics.isEmpty() || topics.size() != topicIds.size()) {
            throw new PetException("存在话题不存在或者已经被删除，无法继续删除");
        }

        Map<Long, Long> postCountMap = this.getPostCount(topicIds);
        boolean isZero = true;
        for (Map.Entry<Long, Long> pair : postCountMap.entrySet()) {
            if (!pair.getValue().equals(0L)) {
                isZero = false;
                break;
            }
        }
        if (!isZero) {
            throw new PetException("部分话题下存在帖子，无法删除");
        }
    }

    private LambdaQueryWrapper<Topic> buildTopicFilterCondition(TopicListFilterDTO topicListFilterDTO) {
        if (topicListFilterDTO == null) {
            return null;
        }

        String topicName = topicListFilterDTO.getTopicName();
        LambdaQueryWrapper<Topic> topicLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(topicName)) {
            topicLambdaQueryWrapper.like(Topic::getName, topicName);
        }

        return topicLambdaQueryWrapper;
    }

    private TopicListVO buildTopicListVOFromTopic(Topic topic, Map<Long, Long> postCountMap) {
        if (topic == null) {
            return null;
        }

        Long topicId = topic.getId();
        String topicName = topic.getName();

        return TopicListVO.builder()
                .topicId(topicId).topicName(topicName)
                .postCount(postCountMap.get(topicId))
                .build();
    }
}
