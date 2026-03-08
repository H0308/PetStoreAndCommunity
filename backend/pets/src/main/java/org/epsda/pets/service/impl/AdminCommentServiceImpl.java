package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.vo.CommentListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.ReplyListVO;
import org.epsda.pets.service.AdminCommentService;
import org.epsda.pets.service.CommentService;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.third.TrieHolder;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/13
 * Time: 16:22
 *
 * @Author: 憨八嘎
 */
@Slf4j
@Service
public class AdminCommentServiceImpl implements AdminCommentService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentSuperMapper commentSuperMapper;
    @Autowired
    private CommentMediaMapper commentMediaMapper;
    @Autowired
    private ProductCommentSubMapper productCommentSubMapper;
    @Autowired
    private MessagePushService messagePushService;

    // 用户服务
    @Autowired
    private CommentService commentService;

    @Override
    public PageVO<CommentListVO> list(CommentListDTO commentListDTO,
                                      CommentListFilterDTO commentListFilterDTO) {
        if (commentListDTO == null) {
            throw new PetException("评论信息错误，获取评论列表失败");
        }

        SecurityUtil.checkAdmin(commentListDTO.getUserId());

        Long currentPage = commentListDTO.getCurrentPage();
        Long pageSize = commentListDTO.getPageSize();
        Page<CommentSuper> commentSuperPage = new Page<>(currentPage, pageSize);

        LambdaQueryWrapper<CommentSuper> commentSuperLambdaQueryWrapper =
                this.buildCommentListFilterCondition(commentListFilterDTO);
        // 用户端为了保证评论内容的可见性，对删除的评论特殊处理
        // 管理员端同样显示已经删除的评论

        Page<CommentSuper> commentSuperPages =
                commentSuperMapper.selectPage(commentSuperPage, commentSuperLambdaQueryWrapper);
        List<CommentSuper> records = commentSuperPages.getRecords();
        List<CommentListVO> commentListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> userIds = records.stream().map(CommentSuper::getUserId).toList();
            List<Long> commentIds = records.stream().map(CommentSuper::getId).toList();
            // 筛选出商品ID
            List<Long> productIds = records.stream()
                    .filter(
                            commentSuper ->
                                    commentSuper.getType().equals(Constants.PRODUCT_COMMENT))
                    .map(CommentSuper::getObjectId).toList();
            // 筛选出帖子ID
            List<Long> postIds = records.stream()
                    .filter(
                            commentSuper ->
                                    commentSuper.getType().equals(Constants.POST_COMMENT))
                    .map(CommentSuper::getObjectId).toList();
            // 筛选出评论类型为商品评论
            List<Long> productCommentIds = records.stream()
                    .filter(
                            commentSuper ->
                                    commentSuper.getType().equals(Constants.PRODUCT_COMMENT))
                    .map(CommentSuper::getId).toList();
            // 筛选出当前评论结果集中回复过的所有父级评论ID
            List<Long> replyCommentIds = records.stream().map(CommentSuper::getParentId).toList();
            // 建立用户ID和用户映射Map
            Map<Long, User> userMap = this.getUserMap(userIds);
            // 建立商品对象Id和商品映射Map
            // 因为管理员后端需要看到已经删除的评论，但是删除的评论可能对应的商品或者帖子也被删除了
            // 所以需要确保商品和帖子查询时不能携带删除判断
            Map<Long, ProductSuper> productIdProductSuperMap = new HashMap<>();
            if (!productIds.isEmpty()) {
                List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                        .in(ProductSuper::getId, productIds));
                productSupers.forEach(productSuper -> productIdProductSuperMap.put(productSuper.getId(), productSuper));
            }
            // 建立商品评论Id和评论子表映射Map
            Map<Long, ProductCommentSub> commentIdProductCommentSubMap = new HashMap<>();
            if (!productIds.isEmpty()) {
                // 使用商品评论ID，使用这些ID到子表中查询确保结果集一定都是来自子表的
                List<ProductCommentSub> productCommentSubs = productCommentSubMapper
                        .selectList(new LambdaQueryWrapper<ProductCommentSub>().in(ProductCommentSub::getCommentId, productCommentIds));
                productCommentSubs.forEach(productCommentSub ->
                        commentIdProductCommentSubMap.put(productCommentSub.getCommentId(), productCommentSub));
            }
            // 建立帖子对象Id和帖子映射Map
            Map<Long, Post> postIdPostMap = new HashMap<>();
            if (!postIds.isEmpty()) {
                List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>().in(Post::getId, postIds));
                posts.forEach(post -> postIdPostMap.put(post.getId(), post));
            }
            // 建立评论Id和评论媒体数量映射Map
            Map<Long, Long> commentIdMediaCountMap = new HashMap<>();
            // 建立评论Id和评论媒体映射Map
            Map<Long, List<CommentMedia>> commentMediaMap = new HashMap<>();
            commentIds.forEach(commentId -> commentIdMediaCountMap.put(commentId, 0L));
            // 此处媒体不考虑删除标记，是为了确保显示的评论是完整的
            // 因为用户只能发布评论和删除评论，评论一旦发布，媒体就是固定无法修改的
            // 只有评论删除或者评论对象删除时，评论媒体才会连带删除
            List<CommentMedia> commentMedia = commentMediaMapper.selectList(new LambdaQueryWrapper<CommentMedia>()
                    .in(CommentMedia::getCommentId, commentIds));
            for (CommentMedia media : commentMedia) {
                Long commentId = media.getCommentId();
                commentIdMediaCountMap.compute(commentId, (key, mediaCount) -> mediaCount + 1);
                if (!commentMediaMap.containsKey(commentId)) {
                    // 第一次插入
                    commentMediaMap.put(commentId, List.of(media));
                } else {
                    List<CommentMedia> mediaList = new ArrayList<>(commentMediaMap.get(commentId));
                    mediaList.add(media);
                    commentMediaMap.put(commentId, mediaList);
                }
            }
            Map<Long, Long> commentIdReplyCountMap = this.getReplyCountMap(commentIds);
            // 建立评论Id和评论对象映射Map
            // 需要注意，此处需要查询当前评论结果集回复过的评论，防止出现当前页的评论不在当前页导致空指针
            Map<Long, CommentSuper> commentSuperMap = new HashMap<>();
            List<CommentSuper> replyCommentSupers = commentSuperMapper.selectList(new LambdaQueryWrapper<CommentSuper>()
                    .in(CommentSuper::getId, replyCommentIds));
            for (CommentSuper commentSuper : replyCommentSupers) {
                commentSuperMap.put(commentSuper.getId(), commentSuper);
            }

            records.forEach(record ->
                    commentListVOS.add(this.buildCommentListVOFromCommentSuper(record, userMap, productIdProductSuperMap,
                            commentIdProductCommentSubMap, postIdPostMap, commentIdMediaCountMap, commentMediaMap,
                            commentIdReplyCountMap, commentSuperMap)));
        }

        return PageVO.<CommentListVO>builder()
                .currentPage(commentSuperPages.getCurrent())
                .totalPages(commentSuperPages.getPages())
                .totalCount(commentSuperPages.getTotal())
                .totalRecords(commentListVOS)
                .build();
    }

    @Override
    public PageVO<ReplyListVO> getReply(ReplyListDTO replyListDTO) {
        if (replyListDTO == null) {
            throw new PetException("评论信息错误，获取回复失败");
        }

        SecurityUtil.checkAdmin(replyListDTO.getUserId());
        Long currentPage = replyListDTO.getCurrentPage();
        Long pageSize = replyListDTO.getPageSize();
        Long parentId = replyListDTO.getParentId();
        Page<CommentSuper> commentSuperPage = new Page<>(currentPage, pageSize);
        Page<CommentSuper> commentSuperPages = commentSuperMapper.selectPage(commentSuperPage,
                new LambdaQueryWrapper<CommentSuper>().eq(CommentSuper::getParentId, parentId));

        List<CommentSuper> records = commentSuperPage.getRecords();
        List<ReplyListVO> replyListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> commentIds = records.stream().map(CommentSuper::getId).toList();
            List<Long> userIds = records.stream().map(CommentSuper::getUserId).toList();
            // 获取用户映射表
            Map<Long, User> userMap = this.getUserMap(userIds);
            // 获取评论个数
            Map<Long, Long> replyCountMap = this.getReplyCountMap(commentIds);
            // 获取媒体
            Map<Long, List<CommentMedia>> commentMediaMap = new HashMap<>();
            List<CommentMedia> commentMedia = commentMediaMapper.selectList(new LambdaQueryWrapper<CommentMedia>()
                    .in(CommentMedia::getCommentId, commentIds));
            for (CommentMedia media : commentMedia) {
                Long commentId = media.getCommentId();
                if (!commentMediaMap.containsKey(commentId)) {
                    // 第一次插入
                    commentMediaMap.put(commentId, List.of(media));
                } else {
                    List<CommentMedia> mediaList = new ArrayList<>(commentMediaMap.get(commentId));
                    mediaList.add(media);
                    commentMediaMap.put(commentId, mediaList);
                }
            }
            records.forEach(record -> replyListVOS.add(
                    this.buildReplyListVOFromCommentSuper(record, userMap, commentMediaMap, replyCountMap)));
        }

        return PageVO.<ReplyListVO>builder()
                .currentPage(commentSuperPages.getCurrent())
                .totalPages(commentSuperPages.getPages())
                .totalCount(commentSuperPages.getTotal())
                .totalRecords(replyListVOS)
                .build();
    }

    @Override
    public Boolean delete(CommentDeleteDTO commentDeleteDTO) {
        if (commentDeleteDTO == null) {
            throw new PetException("评论信息错误，删除评论失败");
        }

        SecurityUtil.checkAdmin(commentDeleteDTO.getUserId());

        // 根据评论类型调用用户层不同的删除评论方法
        Integer commentType = commentDeleteDTO.getCommentType();
        Long commentId = commentDeleteDTO.getCommentId();
        Long userIdComment = commentDeleteDTO.getUserIdComment();
        Boolean deleteRet = true;
        if (commentType.equals(Constants.PRODUCT_COMMENT)) {
            deleteRet = commentService.deleteProductComment(commentId, userIdComment);
        } else if (commentType.equals(Constants.POST_COMMENT)) {
            deleteRet = commentService.deletePostComment(commentId, userIdComment);
        }

        return deleteRet;
    }

    @Transactional
    @Override
    public Boolean batchDelete(CommentBatchDeleteDTO commentBatchDeleteDTO) {
        if (commentBatchDeleteDTO == null ||
            commentBatchDeleteDTO.getCommentIds().isEmpty()) {
            throw new PetException("评论信息错误，批量删除评论失败");
        }

        SecurityUtil.checkAdmin(commentBatchDeleteDTO.getUserId());
        Map<Long, Integer> commentIds = commentBatchDeleteDTO.getCommentIds();
        List<Long> allCommentIds = new ArrayList<>(); // 用于删除媒体和所有评论
        List<Long> productCommentIds = new ArrayList<>(); // 用于匹配评论子表
        // 筛选出帖子评论和商品评论
        for (var commentId : commentIds.entrySet()) {
            Long id = commentId.getKey();
            Integer type = commentId.getValue();
            if (type.equals(Constants.PRODUCT_COMMENT)) {
                productCommentIds.add(id);
            }
            allCommentIds.add(id);
        }

        // 删除所有评论
        int commentUpdateRet = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                .in(CommentSuper::getId, allCommentIds)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .set(CommentSuper::getDeleteFlag, Constants.DELETED_FLAG));
        if (commentUpdateRet != allCommentIds.size()) {
            throw new PetException("存在评论删除失败");
        }
        // 删除评论子表
        try {
            productCommentSubMapper.delete(new LambdaQueryWrapper<ProductCommentSub>()
                    .in(ProductCommentSub::getCommentId, productCommentIds));
        } catch (Exception e) {
            throw new PetException("存在打分量删除失败");
        }
        // 删除媒体
        try {
            commentMediaMapper.update(new LambdaUpdateWrapper<CommentMedia>()
                    .in(CommentMedia::getCommentId, allCommentIds)
                    .eq(CommentMedia::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                    .set(CommentMedia::getDeleteFlag, Constants.DELETED_FLAG));
        } catch (Exception e) {
            throw new PetException("存在评论媒体删除失败");
        }

        return true;
    }

    @Override
    public Boolean verify(CommentVerifyDTO commentVerifyDTO) {
        if (commentVerifyDTO == null) {
            throw new PetException("评论信息错误，审核失败");
        }

        SecurityUtil.checkAdmin(commentVerifyDTO.getUserId());
        Long commentId = commentVerifyDTO.getCommentId();
        Integer opFlag = commentVerifyDTO.getOpFlag();

        CommentSuper commentSuper = commentSuperMapper.selectOne(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getId, commentId)
                .ne(CommentSuper::getStatus, Constants.COMMENT_ON_AUDIT)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (commentSuper == null) {
            throw new PetException("评论不存在或者评论已经通过审核");
        }

        boolean updateRet = true;
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setReceiverId(commentSuper.getUserId());
        commonMessage.setTitle(Constants.AUDIT_SYSTEM_MESSAGE_TITLE);
        String content = "您的评论通过审核，已对外可见。";
        commonMessage.setContent(content);
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.AUDIT_SYSTEM_MESSAGE);
        Long parentId = commentSuper.getParentId();
        if (parentId != null && parentId != 0) {
            CommentSuper parentComment = commentSuperMapper.selectById(parentId);
            User user = userMapper.selectById(commentSuper.getUserId());
            // 自己给自己回复不发通知
            if (!parentComment.getUserId().equals(commentSuper.getUserId())) {
                CommonMessage replyCommonMessage = new CommonMessage();
                replyCommonMessage.setReceiverId(parentComment.getUserId());
                replyCommonMessage.setTitle(Constants.REPLY_SYSTEM_MESSAGE_TITLE);
                if (parentComment.getType().equals(Constants.POST_COMMENT)) {
                    content = user.getUsername() + "回复了你的帖子评论：" + parentComment.getContent();
                } else if (parentComment.getType().equals(Constants.PRODUCT_COMMENT)) {
                    content = user.getUsername() + "回复了你的商品评论：" + parentComment.getContent();
                }
                replyCommonMessage.setContent(content);
                replyCommonMessage.setType(Constants.SYSTEM_MESSAGE);
                messagePushService.saveAndSendSystemMessage(replyCommonMessage, Constants.LIKE_REPLY_SYSTEM_MESSAGE);
            }
        }
        if (opFlag == 0) {
            updateRet = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                    .eq(CommentSuper::getId, commentId)
                    .set(CommentSuper::getStatus, Constants.COMMENT_AUDIT_FAIL)) == 1;
        } else if (opFlag == 1) {
            updateRet = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                    .eq(CommentSuper::getId, commentId)
                    .set(CommentSuper::getStatus, Constants.COMMENT_AUDIT_PASS)) == 1;
        }

        if (!updateRet) {
            throw new PetException("评论更新失败");
        }

        return true;
    }

    private Map<Long, User> getUserMap(List<Long> userIds) {
        // 建立用户ID和用户对象映射Map
        Map<Long, User> userMap = new HashMap<>();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds));
        users.forEach(user -> userMap.put(user.getId(), user));
        return userMap;
    }

    private Map<Long, Long> getReplyCountMap(List<Long> replyCommentIds) {
        // 建立评论Id和回复数量映射Map
        Map<Long, Long> commentIdReplyCountMap = new HashMap<>();
        replyCommentIds.forEach(commentId -> commentIdReplyCountMap.put(commentId, 0L));
        // 需要注意，此处的含义是获取parentId = xxx的所有评论，也就是说，拿到的集合中存储的都是回复过指定评论的回复
        List<CommentSuper> replyComment = commentSuperMapper.selectList(new LambdaQueryWrapper<CommentSuper>()
                .in(CommentSuper::getParentId, replyCommentIds));
        // 统计回复数量，key是被回复的对象，而不是回复的对象
        // 具体取值时，也是根据指定的评论Id获取该评论下的回复个数
        // 而不是获取该评论回复的评论个数
        for (CommentSuper commentSuper : replyComment) {
            commentIdReplyCountMap.compute(commentSuper.getParentId(), (key, replyCount) -> replyCount + 1);
        }
        return commentIdReplyCountMap;
    }


    private ReplyListVO buildReplyListVOFromCommentSuper(CommentSuper commentSuper,
                                                         Map<Long, User> userMap,
                                                         Map<Long, List<CommentMedia>> commentMediaMap,
                                                         Map<Long, Long> replyCountMap) {
        if (commentSuper == null) {
            return null;
        }

        Long commentId = commentSuper.getId();
        Long userId = commentSuper.getUserId();

        // 此处依旧不考虑用户是否删除或者注销，保证评论有效性

        List<String> mediaUrls = new ArrayList<>();
        if (!commentMediaMap.isEmpty()) {
            List<CommentMedia> commentMedia = commentMediaMap.get(commentId);
            if (commentMedia != null && !commentMedia.isEmpty()) {
                mediaUrls = commentMedia.stream().map(CommentMedia::getMediaUrl).toList();
            }
        }
        Long replyCount = 0L;
        if (!replyCountMap.isEmpty()) {
            replyCount = replyCountMap.get(commentId);
        }

        return ReplyListVO.builder().commentId(commentId)
                .userId(userId).username(userMap.get(userId).getUsername())
                .avatarUrl(userMap.get(userId).getAvatarUrl()).content(commentSuper.getContent())
                .mediaUrls(mediaUrls).replyCount(replyCount).status(commentSuper.getStatus())
                .deleteFlag(commentSuper.getDeleteFlag()).createTime(commentSuper.getCreateTime())
                .build();
    }

    private CommentListVO buildCommentListVOFromCommentSuper(CommentSuper commentSuper, Map<Long, User> userMap,
                                                            Map<Long, ProductSuper> productIdProductSuperMap,
                                                            Map<Long, ProductCommentSub> commentIdProductCommentSubMap,
                                                            Map<Long, Post> postIdPostMap,
                                                            Map<Long, Long> commentIdMediaCountMap,
                                                            Map<Long, List<CommentMedia>> commentMediaMap,
                                                            Map<Long, Long> commentIdReplyCountMap,
                                                            Map<Long, CommentSuper> commentSuperMap) {
        if (commentSuper == null) {
            return null;
        }

        Long commentId = commentSuper.getId();
        Long userId = commentSuper.getUserId();
        Long objectId = commentSuper.getObjectId();
        Integer commentType = commentSuper.getType();

        // 此处依旧不考虑用户是否删除或者注销，保证评论有效性

        // 不考虑评论对象不存在（已删除）
        // 因为评论删除可能是随着评论对象的删除而删除
        // 而不是因为违规而删除
        // 为了能正确获取到评论，对应的评论对象也需要显示
        String objectName = "";
        Long stars = null;
        if (commentType.equals(Constants.PRODUCT_COMMENT) && !productIdProductSuperMap.isEmpty()){
            objectName = productIdProductSuperMap.get(objectId).getName();
            if (!commentIdProductCommentSubMap.isEmpty()) {
                ProductCommentSub productCommentSub = commentIdProductCommentSubMap.get(commentId);
                if (productCommentSub != null) {
                    stars = productCommentSub.getStars();
                }
            }
        } else if (commentType.equals(Constants.POST_COMMENT) && !postIdPostMap.isEmpty()) {
            objectName = postIdPostMap.get(objectId).getTitle();
        }

        Long mediaCount = 0L;
        List<String> mediaUrls = new ArrayList<>();
        if (!commentIdMediaCountMap.isEmpty() && !commentMediaMap.isEmpty()) {
            mediaCount = commentIdMediaCountMap.get(commentId);
            List<CommentMedia> commentMedia = commentMediaMap.get(commentId);
            if (commentMedia != null && !commentMedia.isEmpty()) {
                mediaUrls = commentMedia.stream().map(CommentMedia::getMediaUrl).toList();
            }
        }
        Long replyCount = 0L;
        if (!commentIdReplyCountMap.isEmpty()) {
            replyCount = commentIdReplyCountMap.get(commentId);
        }

        Long parentId = commentSuper.getParentId();
        String parentContent = "";
        if (parentId != null && !commentSuperMap.isEmpty()) {
            parentContent = commentSuperMap.get(parentId).getContent();
        }

        return CommentListVO.builder().commentId(commentId)
                .userId(userId).username(userMap.get(userId).getUsername())
                .avatarUrl(userMap.get(userId).getAvatarUrl())
                .commentType(commentType).stars(stars)
                .objectId(objectId).objectName(objectName)
                .parentId(parentId).parentContent(parentContent)
                .content(commentSuper.getContent()).mediaUrls(mediaUrls)
                .mediaCount(mediaCount).replyCount(replyCount)
                .status(commentSuper.getStatus())
                .deleteFlag(commentSuper.getDeleteFlag())
                .createTime(commentSuper.getCreateTime())
                .build();
    }

    private LambdaQueryWrapper<CommentSuper> buildCommentListFilterCondition(CommentListFilterDTO commentListFilterDTO) {
        if (commentListFilterDTO == null) {
            return null;
        }

        LambdaQueryWrapper<CommentSuper> commentSuperLambdaQueryWrapper = new LambdaQueryWrapper<>();

        Long commentId = commentListFilterDTO.getCommentId();
        if (commentId != null) {
            commentSuperLambdaQueryWrapper.eq(CommentSuper::getId, commentId);
        }

        String content = commentListFilterDTO.getContent();
        if (StringUtils.hasText(content)) {
            commentSuperLambdaQueryWrapper.like(CommentSuper::getContent, content);
        }

        Long objectType = commentListFilterDTO.getObjectType();
        if (objectType != null) {
            commentSuperLambdaQueryWrapper.eq(CommentSuper::getType, objectType);
        }

        String objectName = commentListFilterDTO.getObjectName();
        if (StringUtils.hasText(objectName)) {
            // 先获取到商品评论
            List<ProductSuper> productSupers = productSuperMapper.selectList(new LambdaQueryWrapper<ProductSuper>()
                    .like(ProductSuper::getName, objectName)
                    .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            List<Long> productIds = new ArrayList<>();
            productIds.add(-1L);
            if (!productSupers.isEmpty()) {
                productIds = productSupers.stream().map(ProductSuper::getId).toList();
            }
            // 再获取帖子评论
            List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .like(Post::getTitle, objectName)
                    .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            List<Long> postIds = new ArrayList<>();
            postIds.add(-1L);
            if (!posts.isEmpty()) {
                postIds = posts.stream().map(Post::getId).toList();
            }
            commentSuperLambdaQueryWrapper
                .in(CommentSuper::getObjectId, productIds)
                .eq(CommentSuper::getType, Constants.PRODUCT_COMMENT)
                .or()
                .in(CommentSuper::getObjectId, postIds)
                .eq(CommentSuper::getType, Constants.POST_COMMENT);
        }

        Integer status = commentListFilterDTO.getStatus();
        if (status != null) {
            commentSuperLambdaQueryWrapper.eq(CommentSuper::getStatus, status);
        }

        Integer deleteFlag = commentListFilterDTO.getDeleteFlag();
        if (deleteFlag != null) {
            commentSuperLambdaQueryWrapper.eq(CommentSuper::getDeleteFlag, deleteFlag);
        }

        String username = commentListFilterDTO.getUsername();
        if (StringUtils.hasText(username)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, username));
            List<Long> userIds = new ArrayList<>();
            if (!users.isEmpty()) {
                userIds = users.stream().map(User::getId).toList();
            }
            commentSuperLambdaQueryWrapper.in(CommentSuper::getUserId, userIds);
        }

        LocalDateTime startTime = commentListFilterDTO.getStartTime();
        LocalDateTime endTime = commentListFilterDTO.getEndTime();
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new PetException("开始时间不得晚于结束时间");
            }

            commentSuperLambdaQueryWrapper.between(CommentSuper::getCreateTime, startTime, endTime);
        }

        return commentSuperLambdaQueryWrapper;
    }
}
