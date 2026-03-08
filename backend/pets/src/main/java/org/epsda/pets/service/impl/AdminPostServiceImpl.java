package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.segment.mode.impl.SegmentModes;
import com.github.houbb.segment.support.segment.result.impl.SegmentResultHandlers;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.document.FavoriteDocument;
import org.epsda.pets.pojo.document.PostDocument;
import org.epsda.pets.pojo.dto.PostBatchDeleteDTO;
import org.epsda.pets.pojo.dto.PostListDTO;
import org.epsda.pets.pojo.dto.PostListFilterDTO;
import org.epsda.pets.pojo.dto.PostVerifyDTO;
import org.epsda.pets.pojo.vo.AdminPostDetailVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.PostListVO;
import org.epsda.pets.repository.FavoriteRepository;
import org.epsda.pets.repository.PostRepository;
import org.epsda.pets.service.AdminPostService;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.service.PostService;
import org.epsda.pets.third.TrieHolder;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/08
 * Time: 19:55
 *
 * @Author: 憨八嘎
 */
@Slf4j
@Service
public class AdminPostServiceImpl implements AdminPostService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ColumnInfoMapper columnInfoMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private TopicPostMapper topicPostMapper;
    @Autowired
    private PostMediaMapper postMediaMapper;
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private MessagePushService messagePushService;

    // 用户服务
    @Autowired
    private PostService postService;

    @Override
    public PageVO<PostListVO> list(PostListDTO postListDTO, PostListFilterDTO postListFilterDTO) {
        if (postListDTO == null) {
            throw new PetException("帖子信息错误，获取帖子列表失败");
        }

        SecurityUtil.checkAdmin(postListDTO.getUserId());

        Long currentPage = postListDTO.getCurrentPage();
        Long pageSize = postListDTO.getPageSize();
        Page<Post> postPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Post> postLambdaQueryWrapper = this.buildPostListFilterCondition(postListFilterDTO);
        postLambdaQueryWrapper.eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        Page<Post> postPages = postMapper.selectPage(postPage, postLambdaQueryWrapper);
        List<Post> records = postPages.getRecords();
        List<PostListVO> postListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> userIds = records.stream().map(Post::getUserId).toList();
            List<Long> columnIds = records.stream().map(Post::getColumnId).toList();
            List<Long> postIds = records.stream().map(Post::getId).toList();
            // 构建用户Id和用户的映射Map
            Map<Long, User> userMap = new HashMap<>();
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds));
            users.forEach(user -> userMap.put(user.getId(), user));
            // 构建栏目Id和栏目的映射Map
            Map<Long, ColumnInfo> columnInfoMap = new HashMap<>();
            List<ColumnInfo> columnInfos = columnInfoMapper.selectList(new LambdaQueryWrapper<ColumnInfo>()
                    .in(ColumnInfo::getId, columnIds)
                    .eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            columnInfos.forEach(columnInfo -> columnInfoMap.put(columnInfo.getId(), columnInfo));
            Map<Long, List<Topic>> postIdTopicMap = this.getPostIdTopicMap(postIds);
            records.forEach(record ->
                    postListVOS.add(this.buildPostListVOFromPost(record, userMap, columnInfoMap, postIdTopicMap)));
        }

        return PageVO.<PostListVO>builder()
                .currentPage(postPages.getCurrent())
                .totalPages(postPages.getPages())
                .totalCount(postPages.getTotal())
                .totalRecords(postListVOS)
                .build();
    }

    @Override
    public AdminPostDetailVO detail(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new PetException("帖子信息错误，获取帖子失败");
        }

        SecurityUtil.checkAdmin(userId);

        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (post == null) {
            throw new PetException("指定帖子不存在");
        }

        User user = userMapper.selectById(post.getUserId());
        if (user == null) {
            throw new PetException("指定用户不存在");
        }

        ColumnInfo columnInfo = columnInfoMapper.selectOne(new LambdaQueryWrapper<ColumnInfo>()
                .eq(ColumnInfo::getId, post.getColumnId())
                .eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (columnInfo == null) {
            throw new PetException("指定栏目不存在");
        }

        Map<Long, List<Topic>> postIdTopicMap = this.getPostIdTopicMap(List.of(postId));
        Map<Long, String> topicNames = new HashMap<>();
        if (!postIdTopicMap.isEmpty()) {
            List<Topic> topics = postIdTopicMap.get(postId);
            if (topics != null && !topics.isEmpty()) {
                topics.forEach(topic -> topicNames.put(topic.getId(), topic.getName()));
            }
        }

        Long favorCount = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getCancelFlag, Constants.ON_LIKE_DISLIKE_FLAG));

        List<PostMedia> postMedia = postMediaMapper.selectMediaByPostId(postId);
        List<String> mediaUrls = postMedia.stream().map(PostMedia::getMediaUrl).toList();

        return AdminPostDetailVO.builder().postId(postId)
                .userId(user.getId()).username(user.getUsername()).avatarUrl(user.getAvatarUrl())
                .userStatus(user.getStatus()).banFlag(user.getBanFlag())
                .columnId(columnInfo.getId()).columnName(columnInfo.getName())
                .topicNames(topicNames).title(post.getTitle()).content(post.getContent())
                .favorCount(favorCount).likeCount(post.getLikeCount()).rejectCount(post.getRejectCount())
                .status(post.getStatus()).mediaUrls(mediaUrls)
                .createTime(post.getCreateTime()).updateTime(post.getUpdateTime())
                .build();
    }

    @Override
    public Boolean delete(Long postId, Long postUserId, Long userId) {
        // 调用用户层的删除接口
        if (postId == null || postUserId == null || userId == null) {
            throw new PetException("帖子信息错误，无法删除");
        }

        SecurityUtil.checkAdmin(userId);

        return postService.delete(postUserId, postId);
    }

    @Transactional
    @Override
    public Boolean batchDelete(PostBatchDeleteDTO postBatchDeleteDTO) {
        if (postBatchDeleteDTO == null) {
            throw new PetException("帖子信息错误，无法删除");
        }

        SecurityUtil.checkAdmin(postBatchDeleteDTO.getUserId());

        List<Long> postIds = postBatchDeleteDTO.getPostIds();
        int update = postMapper.update(new LambdaUpdateWrapper<Post>()
                .in(Post::getId, postIds)
                .set(Post::getDeleteFlag, Constants.DELETED_FLAG));

        if (postIds.size() != update) {
            throw new PetException("存在帖子删除失败");
        }

        List<PostDocument> postDocuments = postRepository.findAllByPostIdIn(postIds);
        List<FavoriteDocument> favoriteDocuments = favoriteRepository.findAllByPostIdIn(postIds);
        postRepository.deleteAll(postDocuments);
        favoriteRepository.deleteAll(favoriteDocuments);

        return true;
    }

    @Override
    public Boolean verify(PostVerifyDTO postVerifyDTO) {
        if (postVerifyDTO == null) {
            throw new PetException("帖子信息错误");
        }

        SecurityUtil.checkAdmin(postVerifyDTO.getUserId());

        Post exist = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postVerifyDTO.getPostId())
                .ne(Post::getStatus, Constants.POST_ON_AUDIT)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (exist == null) {
            throw new PetException("帖子不存在或者已经审核通过");
        }

        Integer opFlag = postVerifyDTO.getOpFlag();
        Post post = new Post();
        post.setId(postVerifyDTO.getPostId());
        PostDocument postDocument = postRepository.findPostDocumentByPostId(post.getId());
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiverId(exist.getUserId());
        commonMessage.setTitle(Constants.AUDIT_SYSTEM_MESSAGE_TITLE);
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        if (opFlag.equals(Constants.REFUSE_VERIFY)) {
            // 需要给用户发送审核失败原因通知
            String content = "您的帖子" + "《" + exist.getTitle() + "》" + "未通过审核，建议修改后重新提交。";
            commonMessage.setContent(content);
            post.setStatus(Constants.POST_AUDIT_FAIL);
            postDocument.setStatus(Constants.POST_AUDIT_FAIL);
        } else if (opFlag.equals(Constants.ALLOW_VERIFY)) {
            String content = "您的帖子" + "《" + exist.getTitle() + "》" + "通过审核，已对外可见。";
            commonMessage.setContent(content);
            post.setStatus(Constants.POST_AUDIT_PASS);
            postDocument.setStatus(Constants.POST_AUDIT_PASS);
        }

        boolean updateRet = postMapper.updateById(post) == 1;
        if (!updateRet) {
            throw new PetException("帖子状态更新失败");
        }
        postRepository.save(postDocument);
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.AUDIT_SYSTEM_MESSAGE);

        return true;
    }

    private Map<Long, List<Topic>> getPostIdTopicMap(List<Long> postIds) {
        // 构建帖子Id和话题列表的映射Map
        Map<Long, List<Topic>> postIdTopicMap = new HashMap<>();
        List<TopicPost> topicPosts = topicPostMapper.selectList(new LambdaQueryWrapper<TopicPost>()
                .in(TopicPost::getPostId, postIds).eq(TopicPost::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        // 需要注意，帖子的话题可能会为空，即帖子可能没有话题
        if (!topicPosts.isEmpty()) {
            List<Long> topicIds = topicPosts.stream().map(TopicPost::getTopicId).toList();
            // 构建话题Id和话题对象映射Map
            Map<Long, Topic> topicMap = new HashMap<>();
            List<Topic> topics = topicMapper.selectList(new LambdaQueryWrapper<Topic>()
                    .in(Topic::getId, topicIds).eq(Topic::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            topics.forEach(topic -> topicMap.put(topic.getId(), topic));
            for (TopicPost topicPost : topicPosts) {
                Long postId = topicPost.getPostId();
                Long topicId = topicPost.getTopicId();
                if (!postIdTopicMap.containsKey(postId)) {
                    Topic topic = topicMap.get(topicId);
                    postIdTopicMap.put(postId, List.of(topic));
                } else {
                    List<Topic> topicsList = new ArrayList<>(postIdTopicMap.get(postId));
                    topicsList.add(topicMap.get(topicId));
                    postIdTopicMap.put(postId, topicsList);
                }
            }
        }
        return postIdTopicMap;
    }

    private PostListVO buildPostListVOFromPost(Post post, Map<Long, User> userMap,
                                              Map<Long, ColumnInfo> columnInfoMap,
                                              Map<Long, List<Topic>> postIdTopicMap) {
        if (post == null) {
            return null;
        }
        Long postId = post.getId();
        Long userId = post.getUserId();
        Long columnId = post.getColumnId();
        // 帖子不考虑用户是否删除
        // 在本系统中，用户就算是注销依旧可以正常显示其帖子
        // 所以此处不考虑不选择不存在的用户

        ColumnInfo column = columnInfoMap.get(columnId);
        Map<Long, String> topicNames = new HashMap<>();
        if (!postIdTopicMap.isEmpty()) {
            List<Topic> topics = postIdTopicMap.get(postId);
            if (topics != null && !topics.isEmpty()) {
                topics.forEach(topic -> topicNames.put(topic.getId(), topic.getName()));
            }
        }

        return PostListVO.builder()
                .postId(postId).userId(userMap.get(userId).getId())
                .username(userMap.get(userId).getUsername()).title(post.getTitle())
                .content(post.getContent()).columnId(columnId)
                .columnName(column.getName()).topicNames(topicNames).status(post.getStatus())
                .createTime(post.getCreateTime())
                .build();
    }

    private LambdaQueryWrapper<Post> buildPostListFilterCondition(PostListFilterDTO postListFilterDTO) {
        if (postListFilterDTO == null) {
            return null;
        }

        Long columnId = postListFilterDTO.getColumnId();
        LambdaQueryWrapper<Post> postLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (columnId != null) {
            postLambdaQueryWrapper.eq(Post::getColumnId, columnId);
        }

        String title = postListFilterDTO.getTitle();
        if (StringUtils.hasText(title)) {
            postLambdaQueryWrapper.like(Post::getTitle, title);
        }

        Long userId = postListFilterDTO.getUserId();
        if (userId != null) {
            postLambdaQueryWrapper.eq(Post::getUserId, userId);
        }

        Integer status = postListFilterDTO.getStatus();
        if (status != null) {
            postLambdaQueryWrapper.eq(Post::getStatus, status);
        }

        Long topicId = postListFilterDTO.getTopicId();
        if (topicId != null) {
            List<TopicPost> topicPosts = topicPostMapper.selectTopicsByTopicId(topicId);
            List<Long> postIds = new ArrayList<>();
            if (!topicPosts.isEmpty()) {
                postIds = topicPosts.stream().map(TopicPost::getPostId).toList();
            }
            postLambdaQueryWrapper.in(Post::getId, postIds);
        }

        LocalDateTime startTime = postListFilterDTO.getStartTime();
        LocalDateTime endTime = postListFilterDTO.getEndTime();
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new PetException("开始时间不得晚于结束时间");
            }
            postLambdaQueryWrapper.between(Post::getCreateTime, startTime, endTime);
        }

        return postLambdaQueryWrapper;
    }
}
