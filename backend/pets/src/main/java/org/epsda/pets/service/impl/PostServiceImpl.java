package org.epsda.pets.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.executor.BatchResult;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.document.FavoriteDocument;
import org.epsda.pets.pojo.document.PostDocument;
import org.epsda.pets.pojo.dto.*;
import org.epsda.pets.pojo.message.PostAuditInfo;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.repository.FavoriteRepository;
import org.epsda.pets.repository.PostRepository;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.service.PostService;
import org.epsda.pets.utils.JsonUtil;
import org.epsda.pets.utils.OSSUploadFileUtil;
import org.epsda.pets.utils.RecommendUtils;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/18
 * Time: 11:13
 *
 * @Author: 憨八嘎
 */
@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMediaMapper postMediaMapper;
    @Autowired
    private ColumnInfoMapper columnInfoMapper;
    @Autowired
    private CommentSuperMapper commentSuperMapper;
    @Autowired
    private CommentMediaMapper commentMediaMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private TopicPostMapper topicPostMapper;
    @Autowired
    private PostFavoriteMapper postFavoriteMapper;
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private PostDislikeMapper postDislikeMapper;
    @Autowired
    private OSSUploadFileUtil uploadFileUtil;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private MessagePushService messagePushService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ConcurrentHashMap<Long, Long> likeLimitCountMap = new ConcurrentHashMap<>();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void buildPostDataIntoES() {
        // 获取未删除的帖子
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<PostDocument> postDocuments = new ArrayList<>();
        posts.forEach(post -> postDocuments.add(this.buildPostDocumentFromPost(post)));
        postRepository.saveAll(postDocuments);
    }

    @PostConstruct
    public void buildFavoritePostDataIntoES() {
        List<PostFavorite> postFavorites = postFavoriteMapper.selectList(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getCancelFlag, Constants.NOT_DELETED_FLAG));
        List<FavoriteDocument> favoriteDocuments = new ArrayList<>();
        postFavorites.forEach(postFavorite ->
                favoriteDocuments.add(this.buildFavoriteDocumentFromPostFavorite(postFavorite)));
        favoriteRepository.saveAll(favoriteDocuments);
    }

    @Override
    public PageVO<PostVO> list(PostsDTO postsDTO) {
        if (postsDTO == null || postsDTO.getCurrentPage() == null || postsDTO.getPageSize() == null){
            throw new PetException("页面信息错误，获取帖子列表失败");
        }

        Long currentPage = postsDTO.getCurrentPage();
        Long pageSize = postsDTO.getPageSize();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPage = new
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(currentPage, pageSize);
        Long columnId = postsDTO.getColumnId();
        LambdaQueryWrapper<Post> postLambdaQueryWrapper = new LambdaQueryWrapper<>();
        postLambdaQueryWrapper.eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(Post::getStatus, Constants.POST_AUDIT_PASS)
                .eq(columnId != null, Post::getColumnId, columnId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> posts =
                postMapper.selectPage(postPage, postLambdaQueryWrapper);
        List<PostVO> postVOS = new ArrayList<>();
        posts.getRecords().forEach(post -> postVOS.add(this.buildPostVOFromPost(post)));

        return PageVO.<PostVO>builder()
                .currentPage(posts.getCurrent())
                .totalPages(posts.getPages())
                .totalCount(posts.getTotal())
                .totalRecords(postVOS)
                .build();
    }

    @Override
    public List<PostColumnInfosVO> column() {
        String postColumnRet = stringRedisTemplate.opsForValue().get(Constants.REDIS_POST_COLUMN);
        if (postColumnRet != null) {
            if (!StringUtils.hasText(postColumnRet)) {
                throw new PetException("获取栏目列表失败");
            }
            return JsonUtil.toObject(postColumnRet,
                    new TypeReference<List<PostColumnInfosVO>>() {});
        }

        List<ColumnInfo> columnInfos = columnInfoMapper.selectAllColumnInfo();
        if (columnInfos.isEmpty()) {
            // 防止缓存穿透
            stringRedisTemplate.opsForValue().set(Constants.REDIS_POST_COLUMN, "",
                    Constants.NULL_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
            throw new PetException("获取栏目列表失败");
        }
        List<PostColumnInfosVO> postColumnInfosVOS = columnInfos.stream().map(columnInfo ->
                PostColumnInfosVO.builder()
                        .columnId(columnInfo.getId())
                        .name(columnInfo.getName())
                        .build()).toList();
        // 添加到缓存中
        String json = JsonUtil.toJson(postColumnInfosVOS);
        stringRedisTemplate.opsForValue().set(Constants.REDIS_POST_COLUMN, json,
                Constants.COLUMN_EXPIRE_TIME_DAY, TimeUnit.MINUTES);
        return postColumnInfosVOS;
    }

    @Override
    public List<PostTopicInfosVO> topic() {
        List<Topic> topics = topicMapper.selectAllTopicInfo();
        return topics.stream().map(topic ->
                PostTopicInfosVO.builder()
                        .topicId(topic.getId())
                        .name(topic.getName())
                        .build()).toList();
    }

    @Override
    public PageVO<SelfPostVO> getFromUser(SelfPostDTO selfPostDTO, SelfPostFilterDTO selfPostFilterDTO) {
        if (selfPostDTO == null) {
            throw new PetException("帖子信息错误，获取用户帖子失败");
        }

        Long currentPage = selfPostDTO.getCurrentPage();
        Long pageSize = selfPostDTO.getPageSize();
        Long userId = selfPostDTO.getUserId();
        if (userId == null || userId == 0) {
            throw new PetException("用户ID错误，获取用户帖子失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);

        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(Math.toIntExact(currentPage - 1), Math.toIntExact(pageSize), sort);
        String keyword = selfPostFilterDTO.getKeyword();
        Integer status = selfPostFilterDTO.getStatus();
        Page<PostDocument> postDocumentPage;
        if (!StringUtils.hasText(keyword) && status == null) {
            postDocumentPage = postRepository.findPostDocumentByUserId(userId, pageable);
        } else if (!StringUtils.hasText(selfPostFilterDTO.getKeyword())){
            // 筛选状态即可
            postDocumentPage = postRepository.findPostDocumentByStatusAndUserId(status, userId, pageable);
        } else if (selfPostFilterDTO.getStatus() == null) {
            String titleField = (keyword.length() <= 1) ? "title.ngram" : "title";
            String contentField = (keyword.length() <= 1) ? "content.ngram" : "content";
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query -> query
                        .bool(bool -> bool
                            .must(must -> must
                                .multiMatch(multi -> multi
                                    .fields(titleField, contentField)
                                    .query(keyword)))
                            // 过滤指定用户
                            .filter(filter -> filter
                                .term(term -> term
                                    .field("userId")
                                    .value(userId)))))
                    // 按创建时间降序
                    .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                    .build();
            SearchHits<PostDocument> search = elasticsearchOperations.search(nativeQuery, PostDocument.class);
            postDocumentPage = SearchHitSupport.searchPageFor(search, pageable).map(SearchHit::getContent);
        } else {
            String titleField = (keyword.length() <= 1) ? "title.ngram" : "title";
            String contentField = (keyword.length() <= 1) ? "content.ngram" : "content";
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query -> query
                        .bool(bool -> bool
                            .must(must -> must
                                .multiMatch(multi -> multi
                                    .fields(titleField, contentField)
                                    .query(keyword)))
                                .filter(filter -> filter
                                    .term(term -> term
                                        .field("userId")
                                        .value(userId)))
                                .filter(filter -> filter
                                    .term(term -> term
                                        .field("status")
                                        .value(status)))))
                    .withSort(Sort.by(Sort.Direction.DESC, "createTime"))
                    .build();
            SearchHits<PostDocument> search = elasticsearchOperations.search(nativeQuery, PostDocument.class);
            postDocumentPage = SearchHitSupport.searchPageFor(search, pageable).map(SearchHit::getContent);
        }

        List<PostDocument> content = postDocumentPage.getContent();
        List<SelfPostVO> selfPostVOS = new ArrayList<>();
        content.forEach(postDocument -> selfPostVOS.add(this.buildSelfPostVOFromPostDocument(postDocument)));

        return PageVO.<SelfPostVO>builder()
                .currentPage((long) (postDocumentPage.getNumber() + 1))
                .totalPages((long) postDocumentPage.getTotalPages())
                .totalCount(postDocumentPage.getTotalElements())
                .totalRecords(selfPostVOS)
                .build();
    }

    @Override
    public PostDetailVO detail(Long postId) {
        if (postId == null || postId == 0) {
            throw new PetException("帖子ID错误，无法获取帖子详情");
        }

        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG)
        );
        if (post == null) {
            throw new PetException("帖子信息错误，无法获取帖子详情");
        }

        Long userId = post.getUserId();
        // 此处依旧不考虑用户是否被删除，确保帖子可见性
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new PetException("发帖人不存在，无法获取帖子详情");
        }
        String username = user.getUsername();
        String avatarUrl = user.getAvatarUrl();

        List<TopicPost> topicPosts = topicPostMapper.selectTopicsByPostId(postId);
        List<Long> topicIds = new ArrayList<>();
        List<String> topicNames = new ArrayList<>();
        if (!topicPosts.isEmpty()) {
            topicPosts.forEach(topicPost -> {
                Long topicId = topicPost.getTopicId();
                Topic topic = topicMapper.selectById(topicId);
                topicIds.add(topicId);
                topicNames.add(topic.getName());
            });
        }

        Long columnId = post.getColumnId();
        ColumnInfo columnInfo = columnInfoMapper.selectById(columnId);
        Long favorCount = postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>().eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getCancelFlag, Constants.ON_FAVOR_FLAG));

        List<PostMedia> postMedia = postMediaMapper.selectMediaByPostId(postId);
        Map<Long, String> mediaUrlsWithId = new HashMap<>();
        postMedia.forEach(media -> mediaUrlsWithId.put(media.getId(), media.getMediaUrl()));
        return PostDetailVO.builder().postId(postId)
                .userId(userId).username(username).avatar(avatarUrl)
                .topicId(topicIds).topicName(topicNames).favorCount(favorCount)
                .columnId(columnId).columnName(columnInfo.getName())
                .title(post.getTitle()).content(post.getContent())
                .likeCount(post.getLikeCount()).rejectCount(post.getRejectCount())
                .mediaUrlWithId(mediaUrlsWithId).updateTime(post.getUpdateTime())
                .build();
    }

    @Override
    public PageVO<PostVO> getUnderColumn(PostUnderColumnDTO postUnderColumnDTO) {
        if (postUnderColumnDTO == null) {
            throw new PetException("栏目信息错误，无法获取到帖子");
        }

        Long currentPage = postUnderColumnDTO.getCurrentPage();
        Long pageSize = postUnderColumnDTO.getPageSize();
        Long columnId = postUnderColumnDTO.getColumnId();

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(currentPage, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPages =
                postMapper.selectPage(postPage,
                new LambdaQueryWrapper<Post>().eq(Post::getColumnId, columnId)
                        .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                        .eq(Post::getStatus, Constants.POST_AUDIT_PASS));

        List<PostVO> postVOS = new ArrayList<>();
        postPages.getRecords().forEach(post -> postVOS.add(this.buildPostVOFromPost(post)));

        return PageVO.<PostVO>builder()
                .currentPage(postPages.getCurrent())
                .totalPages(postPages.getPages())
                .totalCount(postPages.getTotal())
                .totalRecords(postVOS)
                .build();
    }

    @Override
    public PageVO<PostVO> getUnderTopic(PostUnderTopicDTO postUnderTopicDTO) {
        if (postUnderTopicDTO == null) {
            throw new PetException("话题信息错误，无法获取到帖子");
        }

        Long topicId = postUnderTopicDTO.getTopicId();
        Long pageSize = postUnderTopicDTO.getPageSize();
        Long currentPage = postUnderTopicDTO.getCurrentPage();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(currentPage, pageSize);

        List<TopicPost> topicPosts = topicPostMapper.selectList(new LambdaQueryWrapper<TopicPost>()
                .eq(TopicPost::getTopicId, topicId)
                .eq(TopicPost::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<Long> postIds = new ArrayList<>();
        topicPosts.forEach(topicPost -> {
            Long postId = topicPost.getPostId();
            postIds.add(postId);
        });

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> postPages =
                postMapper.selectPage(postPage, new LambdaQueryWrapper<Post>().in(Post::getId, postIds)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(Post::getStatus, Constants.POST_AUDIT_PASS));
        List<PostVO> postVOS = new ArrayList<>();
        postPages.getRecords().forEach(post -> postVOS.add(this.buildPostVOFromPost(post)));

        return PageVO.<PostVO>builder()
                .currentPage(postPages.getCurrent())
                .totalPages(postPages.getPages())
                .totalCount(postPages.getTotal())
                .totalRecords(postVOS)
                .build();
    }

    @Transactional
    @Override
    public PostTextVO toPost(ToPostDTO toPostDTO, Integer mediaType, List<MultipartFile> files) {
        if (toPostDTO == null) {
            throw new PetException("帖子信息错误，上传帖子失败");
        }

        Long userId = toPostDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(User::getBanFlag, Constants.NOT_BANNED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在或者已经被禁言，无法创建帖子");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        if (!StringUtils.hasText(user.getRealName()) || !StringUtils.hasText(user.getIdCard())) {
            throw new PetException("用户未进行实名认证，无法发帖");
        }
        List<Long> topicIds = toPostDTO.getTopicIds();
        Long columnId = toPostDTO.getColumnId();
        Integer opFlag = toPostDTO.getOpFlag();
        if (columnId == 0 || userId == 0) {
            throw new PetException("帖子信息非法，上传帖子失败");
        }

        String title = toPostDTO.getTitle();
        String content = toPostDTO.getContent();
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new PetException("帖子标题或者内容为空，上传帖子失败");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setColumnId(columnId);
        post.setTitle(title);
        post.setContent(content);
        PostDocument postDocument = PostDocument.builder().build();
        postDocument.setUserId(userId);
        postDocument.setTitle(title);
        postDocument.setContent(content);
        postDocument.setPostMediaUrl(null);
        postDocument.setPostMediaType(null);
        if (opFlag == 0) {
            post.setStatus(Constants.POST_DRAFT); // 默认为草稿状态
            postDocument.setStatus(Constants.POST_DRAFT);
        } else if (opFlag == 1){
            post.setStatus(Constants.POST_ON_AUDIT); // 审核中
            postDocument.setStatus(Constants.POST_ON_AUDIT);
        }

        boolean postInsertRet = postMapper.insert(post) == 1;
        Long postId = 0L;
        if (postInsertRet) {
            postId = post.getId();
        }

        // 如果有话题，需要加入到话题帖子关联表
        List<TopicPost> topicPosts = new ArrayList<>();
        if (topicIds != null && !topicIds.isEmpty()) {
            if (topicIds.size() > 3) {
                throw new PetException("最多选择3个话题");
            }
            for (var topicId : topicIds) {
                TopicPost topicPost = new TopicPost();
                topicPost.setPostId(postId);
                topicPost.setTopicId(topicId);
                topicPosts.add(topicPost);
            }
        }
        if (!topicPosts.isEmpty()) {
            List<BatchResult> insert = topicPostMapper.insert(topicPosts);
        }

        // 插入媒体
        List<String> fileUrls = this.insertPostMedia(postId, mediaType, files);

        postDocument.setPostId(postId);
        postDocument.setCreateTime(post.getCreateTime());
        postDocument.setPostMediaUrl(fileUrls.get(0));
        postDocument.setPostMediaType(uploadFileUtil.getMediaType(files.get(0).getOriginalFilename()));
        postRepository.save(postDocument);

        // 交给RabbitMQ进行自动审核
        if (opFlag == 1) {
            PostAuditInfo postAuditInfo = new PostAuditInfo(postId);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(Constants.AUDIT_EXCHANGE,
                            Constants.POST_AUDIT_ROUTING_KEY, postAuditInfo);
                }
            });
        }

        return PostTextVO.builder()
                .successFlag(postInsertRet)
                .postId(postId)
                .build();
    }

    @Transactional
    @Override
    public PostEditVO edit(PostEditDTO postEditDTO, Integer mediaType, List<MultipartFile> files) {
        if (postEditDTO == null) {
            throw new PetException("帖子信息错误，无法编辑帖子");
        }
        Long userId = postEditDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);

        Long postId = postEditDTO.getPostId();
        Post oldPost = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (oldPost == null) {
            throw new PetException("指定帖子不存在，无法修改帖子");
        }
        if (!oldPost.getUserId().equals(userId)) {
            throw new PetException("帖子归属不正确，修改帖子失败");
        }
        PostDocument postDocument = postRepository.findPostDocumentByPostId(postId);

        Post newPost = new Post();
        newPost.setId(postId);

        // 修改栏目
        Long columnId = postEditDTO.getColumnId();
        if (columnId != null && columnId > 0) {
            ColumnInfo columnInfo = columnInfoMapper.selectOne(new LambdaQueryWrapper<ColumnInfo>()
                    .eq(ColumnInfo::getId, columnId)
                    .eq(ColumnInfo::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            if (columnInfo == null) {
                throw new PetException("栏目不存在，无法修改帖子");
            }
            newPost.setColumnId(columnId);
        }

        // 修改话题
        List<Long> topicIds = postEditDTO.getTopicIds();
        if (topicIds != null && !topicIds.isEmpty()) {
            if (topicIds.size() > 3) {
                throw new PetException("最多选择3个话题");
            }
            // 先判断是否之前已经有设置过话题
            List<TopicPost> topicPosts = topicPostMapper.selectTopicsByPostId(postId);
            if (topicPosts != null && !topicPosts.isEmpty()) {
                // 存在已经设置话题
                // 本次系统为了简单处理，考虑删除原有的话题，再重新新增新的话题
                List<Long> topicDeleteIds = topicPosts.stream().map(TopicPost::getId).toList();
                int update = topicPostMapper.update(new LambdaUpdateWrapper<TopicPost>()
                        .in(TopicPost::getId, topicDeleteIds)
                        .set(TopicPost::getDeleteFlag, Constants.DELETED_FLAG));
            }
            topicPosts = new ArrayList<>();
            for (var topicId : topicIds) {
                TopicPost topicPost = new TopicPost();
                topicPost.setPostId(postId);
                topicPost.setTopicId(topicId);
                topicPosts.add(topicPost);
            }
            if (!topicPosts.isEmpty()) {
                List<BatchResult> insert = topicPostMapper.insert(topicPosts);
            }
        }

        // 修改标题和内容
        String title = postEditDTO.getTitle();
        String content = postEditDTO.getContent();
        if (StringUtils.hasText(title)) {
            newPost.setTitle(title);
            postDocument.setTitle(title);
        }
        if (StringUtils.hasText(content)) {
            newPost.setContent(content);
            postDocument.setContent(content);
        }

        // 修改媒体（先删除原有媒体，再新增媒体）
        Map<Long, Integer> mediaUrlWithDeleteFlag = postEditDTO.getMediaUrlWithDeleteFlag();
        if (mediaUrlWithDeleteFlag != null && !mediaUrlWithDeleteFlag.isEmpty()) {
            List<PostMedia> postMedias = postMediaMapper.selectMediaByPostId(postId);
            List<Long> mediaDeleteIds = postMedias.stream()
                    .map(PostMedia::getId)
                    .filter(mediaId ->
                        mediaUrlWithDeleteFlag.get(mediaId)
                                .equals(Constants.DELETED_FLAG))
                    .toList();
            if (!mediaDeleteIds.isEmpty()) {
                int update = postMediaMapper.update(new LambdaUpdateWrapper<PostMedia>()
                        .in(PostMedia::getId, mediaDeleteIds)
                        .set(PostMedia::getDeleteFlag, Constants.DELETED_FLAG));
            }
        }

        // 新增媒体不一定是用户删除才能新增
        if (mediaType != null && !files.isEmpty()) {
            // 新增媒体
            List<String> fileUrls = this.insertPostMedia(postId, mediaType, files);
            postDocument.setPostMediaUrl(fileUrls.get(0));
            postDocument.setPostMediaType(uploadFileUtil.getMediaType(files.get(0).getOriginalFilename()));
        }

        // 修改帖子状态
        Integer opFlag = postEditDTO.getOpFlag();
        if (opFlag == 0) {
            newPost.setStatus(Constants.POST_DRAFT);
            postDocument.setStatus(Constants.POST_DRAFT);
        } else if (opFlag == 1) {
            newPost.setStatus(Constants.POST_ON_AUDIT);
            postDocument.setStatus(Constants.POST_ON_AUDIT);
            // 交给RabbitMQ审核（事务提交后再发送，避免监听器查到旧状态）
            PostAuditInfo postAuditInfo = new PostAuditInfo(postId);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(Constants.AUDIT_EXCHANGE,
                            Constants.POST_AUDIT_ROUTING_KEY, postAuditInfo);
                }
            });
        }

        boolean updateRet = postMapper.updateById(newPost) == 1;
        if (!updateRet) {
            throw new PetException("新帖更新失败，无法修改帖子");
        }

        // 不需要处理事务问题
        // 如果上面都没有抛出异常，说明正常修改
        // 此时可以直接更新
        postRepository.save(postDocument);

        return PostEditVO.builder()
                .postId(postId).title(title)
                .columnId(columnId).content(content)
                .topicIds(topicIds)
                .build();
    }

    @Transactional
    @Override
    public Long like(PostLikeDTO postLikeDTO) {
        if (postLikeDTO == null || postLikeDTO.getUserId() == null ||
            postLikeDTO.getPostId() == null) {
            throw new PetException("用户ID或者帖子ID为空，无法点赞或取消");
        }

        Long userId = postLikeDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，无法点赞或取消");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long postId = postLikeDTO.getPostId();
        Post oldPost = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (oldPost == null) {
            throw new PetException("帖子不存在，无法点赞或取消");
        }
        Integer opFlag = postLikeDTO.getOpFlag();
        if (opFlag == 1) {
            // 取消点赞
            // 取消点赞一定是建立在用户点赞的前提之下
            // 所以直接取消指定点赞即可
            PostLike postLike = postLikeMapper.selectByUserIdAndPostId(userId, postId);
            if (postLike == null ||
                postLike.getCancelFlag().equals(Constants.CANCEL_LIKE_DISLIKE_FLAG)) {
                throw new PetException("该用户未给指定帖子点赞，无法取消点赞");
            }
            PostLike cancelLike = new PostLike();
            cancelLike.setId(postLike.getId());
            cancelLike.setCancelFlag(Constants.CANCEL_LIKE_DISLIKE_FLAG);
            boolean updateRet = postLikeMapper.updateById(cancelLike) == 1;
            if (!updateRet) {
                throw new PetException("取消点赞失败");
            }
            // 更新点赞值
            Post newPost = new Post();
            newPost.setId(postId);
            newPost.setLikeCount(oldPost.getLikeCount() - 1);
            boolean postUpdateRet = postMapper.updateById(newPost) == 1;
            if (!postUpdateRet) {
                throw new PetException("更新点赞值失败");
            }
        } else if (opFlag == 0) {
            // 点赞
            // 先判断用户是否已经给指定帖子点踩
            PostDislike postDislike = postDislikeMapper.selectByUserIdAndPostId(userId, postId);
            if (postDislike != null && postDislike.getCancelFlag().equals(Constants.ON_LIKE_DISLIKE_FLAG)) {
                // 先取消点踩
                PostDislike cancelDislike = new PostDislike();
                cancelDislike.setId(postDislike.getId());
                cancelDislike.setCancelFlag(Constants.CANCEL_LIKE_DISLIKE_FLAG);
                boolean dislikeUpdateRet = postDislikeMapper.updateById(cancelDislike) == 1;
                if (!dislikeUpdateRet) {
                    throw new PetException("取消点踩失败");
                }
                // 更新点踩值
                Post newPost = new Post();
                newPost.setId(postId);
                newPost.setRejectCount(oldPost.getRejectCount() - 1);
                boolean postUpdateRet = postMapper.updateById(newPost) == 1;
                if (!postUpdateRet) {
                    throw new PetException("更新点踩值失败");
                }
            }
            // 再进行点赞
            PostLike postLike = postLikeMapper.selectByUserIdAndPostId(userId, postId);
            PostLike like = new PostLike();
            if (postLike != null) {
                // 直接更新标记
                like.setId(postLike.getId());
                like.setCancelFlag(Constants.ON_LIKE_DISLIKE_FLAG);
                boolean updateRet = postLikeMapper.updateById(like) == 1;
                if (!updateRet) {
                    throw new PetException("恢复点赞失败");
                }
            } else {
                // 否则插入新的数据
                like.setUserId(userId);
                like.setPostId(postId);
                boolean insertRet = postLikeMapper.insert(like) == 1;
                if (!insertRet) {
                    throw new PetException("点赞失败");
                }
            }

            // 更新点赞值
            Post newPost = new Post();
            newPost.setId(postId);
            newPost.setLikeCount(oldPost.getLikeCount() + 1);
            boolean postUpdateRet = postMapper.updateById(newPost) == 1;
            if (!postUpdateRet) {
                throw new PetException("更新点赞值失败");
            }

            // 确保每个用户只在第一次点赞的时候发送通知，并且自己点赞不发通知
            if (!likeLimitCountMap.containsKey(userId) &&
                    !oldPost.getUserId().equals(userId)) {
                CommonMessage replyCommonMessage = new CommonMessage();
                replyCommonMessage.setReceiverId(oldPost.getUserId());
                replyCommonMessage.setTitle(Constants.LIKE_SYSTEM_MESSAGE_TITLE);
                String content = user.getUsername() + "点赞了你的帖子" + "《" + oldPost.getTitle() + "》";
                replyCommonMessage.setContent(content);
                replyCommonMessage.setType(Constants.SYSTEM_MESSAGE);
                messagePushService.saveAndSendSystemMessage(replyCommonMessage,
                        Constants.LIKE_REPLY_SYSTEM_MESSAGE);
                likeLimitCountMap.put(userId, postId);
            }
        }

        // 获取最新的点赞值返回
        Post post = postMapper.selectById(postId);
        return post.getLikeCount();
    }

    @Transactional
    @Override
    public Long dislike(PostDislikeDTO postDislikeDTO) {
        if (postDislikeDTO == null || postDislikeDTO.getUserId() == null ||
                postDislikeDTO.getPostId() == null) {
            throw new PetException("用户ID或者帖子ID为空，无法点踩或取消");
        }

        Long userId = postDislikeDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，无法点踩或取消");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long postId = postDislikeDTO.getPostId();
        Post oldPost = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (oldPost == null) {
            throw new PetException("帖子不存在，无法点踩或取消");
        }
        Integer opFlag = postDislikeDTO.getOpFlag();
        if (opFlag == 1) {
            // 取消点踩
            PostDislike postDislike = postDislikeMapper.selectByUserIdAndPostId(userId, postId);
            if (postDislike == null ||
                    postDislike.getCancelFlag().equals(Constants.CANCEL_LIKE_DISLIKE_FLAG)) {
                throw new PetException("该用户未给指定帖子点踩，无法取消点踩");
            }
            PostDislike cancelDislike = new PostDislike();
            cancelDislike.setId(postDislike.getId());
            cancelDislike.setCancelFlag(Constants.CANCEL_LIKE_DISLIKE_FLAG);
            boolean updateRet = postDislikeMapper.updateById(cancelDislike) == 1;
            if (!updateRet) {
                throw new PetException("取消点踩失败");
            }
            // 更新点踩值
            Post newPost = new Post();
            newPost.setId(postId);
            newPost.setRejectCount(oldPost.getRejectCount() - 1);
            boolean postUpdateRet = postMapper.updateById(newPost) == 1;
            if (!postUpdateRet) {
                throw new PetException("更新点踩值失败");
            }
        } else if (opFlag == 0){
            // 点踩
            PostLike postLike = postLikeMapper.selectByUserIdAndPostId(userId, postId);
            if (postLike != null && postLike.getCancelFlag().equals(Constants.ON_LIKE_DISLIKE_FLAG)) {
                // 先取消点赞
                PostLike cancelLike = new PostLike();
                cancelLike.setId(postLike.getId());
                cancelLike.setCancelFlag(Constants.CANCEL_LIKE_DISLIKE_FLAG);
                boolean likeUpdateRet = postLikeMapper.updateById(cancelLike) == 1;
                if (!likeUpdateRet) {
                    throw new PetException("取消点赞失败");
                }
                // 更新点赞值
                Post newPost = new Post();
                newPost.setId(postId);
                newPost.setLikeCount(oldPost.getLikeCount() - 1);
                boolean postUpdateRet = postMapper.updateById(newPost) == 1;
                if (!postUpdateRet) {
                    throw new PetException("更新点赞值失败");
                }
            }
            // 再进行点踩
            PostDislike postDislike = postDislikeMapper.selectByUserIdAndPostId(userId, postId);
            PostDislike dislike = new PostDislike();
            if (postDislike != null) {
                // 直接更新标记
                dislike.setId(postDislike.getId());
                dislike.setCancelFlag(Constants.ON_LIKE_DISLIKE_FLAG);
                boolean updateRet = postDislikeMapper.updateById(dislike) == 1;
                if (!updateRet) {
                    throw new PetException("恢复点踩失败");
                }
            } else {
                // 否则插入新的数据
                dislike.setUserId(userId);
                dislike.setPostId(postId);
                boolean insertRet = postDislikeMapper.insert(dislike) == 1;
                if (!insertRet) {
                    throw new PetException("点踩失败");
                }
            }

            // 更新点踩值
            Post newPost = new Post();
            newPost.setId(postId);
            newPost.setRejectCount(oldPost.getRejectCount() + 1);
            boolean postUpdateRet = postMapper.updateById(newPost) == 1;
            if (!postUpdateRet) {
                throw new PetException("更新点踩值失败");
            }
        }

        // 获取最新的点踩值返回
        Post post = postMapper.selectById(postId);
        return post.getRejectCount();
    }

    @Override
    public Boolean getLike(Long userId, Long postId) {
        if (userId == null || postId == null) {
            throw new PetException("用户ID或者帖子ID错误，获取点赞状态失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        PostLike postLike = postLikeMapper.selectByUserIdAndPostId(userId, postId);
        return postLike != null && postLike.getCancelFlag().equals(Constants.ON_LIKE_DISLIKE_FLAG);
    }

    @Override
    public Boolean getDislike(Long userId, Long postId) {
        if (userId == null || postId == null) {
            throw new PetException("用户ID或者帖子ID错误，获取点赞状态失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        PostDislike postDislike = postDislikeMapper.selectByUserIdAndPostId(userId, postId);
        return postDislike != null && postDislike.getCancelFlag().equals(Constants.ON_LIKE_DISLIKE_FLAG);
    }

    @Override
    public PageVO<FavoritePostVO> favorite(FavoritePostDTO favoritePostDTO) {
        if (favoritePostDTO == null) {
            throw new PetException("帖子收藏信息错误，获取收藏失败");
        }

        Long currentPage = favoritePostDTO.getCurrentPage();
        Long pageSize = favoritePostDTO.getPageSize();
        Long userId = favoritePostDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 构建查询和分页
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        Pageable pageable = PageRequest.of(Math.toIntExact(currentPage - 1),
                Math.toIntExact(pageSize), sort);
        String keyword = favoritePostDTO.getKeyword();
        Page<FavoriteDocument> favoriteDocumentPage;
        if (!StringUtils.hasText(keyword)) {
            // 直接获取所有数据
            favoriteDocumentPage = favoriteRepository.findFavoriteDocumentByFavorUserId(userId, pageable);
        } else {
            String titleField = (keyword.length() <= 1) ? "title.ngram" : "title";
            String contentField = (keyword.length() <= 1) ? "content.ngram" : "content";
            String postUsernameField = (keyword.length() <= 1) ? "postUsername.ngram" : "postUsername";
            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                    .bool(bool -> bool
                        .must(must -> must
                            .multiMatch(multi -> multi
                                .fields(titleField, contentField, postUsernameField)
                                .query(keyword)))
                        // 过滤指定用户
                        .filter(filter -> filter
                            .term(term -> term
                                .field("favorUserId")
                                .value(userId)))))
                    // 按创建时间降序
                    .withSort(Sort.by(Sort.Direction.DESC, "updateTime"))
                    .build();
            SearchHits<FavoriteDocument> search = elasticsearchOperations.search(nativeQuery, FavoriteDocument.class);
            favoriteDocumentPage = SearchHitSupport.searchPageFor(search, pageable).map(SearchHit::getContent);
        }

        List<FavoriteDocument> content = favoriteDocumentPage.getContent();
        List<FavoritePostVO> favoritePostVOS = new ArrayList<>();
        content.forEach(favoriteDocument ->
                favoritePostVOS.add(this.buildFavoritePostVOFromFavoriteDocument(favoriteDocument)));

        return PageVO.<FavoritePostVO>builder()
                .currentPage((long) (favoriteDocumentPage.getNumber() + 1))
                .totalPages((long) favoriteDocumentPage.getTotalPages())
                .totalCount(favoriteDocumentPage.getTotalElements())
                .totalRecords(favoritePostVOS)
                .build();
    }

    @Override
    public Boolean getFavor(Long userId, Long postId) {
        if (userId == null || postId == null) {
            throw new PetException("用户ID或者帖子ID错误，获取收藏状态失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        PostFavorite postFavorite = postFavoriteMapper.selectByUserIdAndPostId(userId, postId);
        return postFavorite != null && postFavorite.getCancelFlag().equals(Constants.ON_FAVOR_FLAG);
    }

    @Transactional
    @Override
    public Long favor(FavorPostDTO favorPostDTO) {
        if (favorPostDTO == null) {
            throw new PetException("收藏信息错误，修改收藏失败");
        }

        Long postId = favorPostDTO.getPostId();
        // 这里不考虑帖子是否被删除，因为可能帖子在收藏期间被删除了
        // 需要给用户提供一个移除帖子的入口
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId));
        if (post == null) {
            throw new PetException("帖子不存在，无法修改收藏");
        }
        Long userId = favorPostDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在，无法修改收藏");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Integer opFlag = favorPostDTO.getOpFlag();
        if (opFlag == 1) {
            // 取消收藏
            PostFavorite postFavorite = postFavoriteMapper.selectByUserIdAndPostId(userId, postId);
            if (postFavorite == null || postFavorite.getCancelFlag().equals(Constants.CANCEL_FAVOR_FLAG)) {
                throw new PetException("当前帖子未被收藏，取消收藏失败");
            }
            PostFavorite cancelPostFavorite = new PostFavorite();
            cancelPostFavorite.setId(postFavorite.getId());
            cancelPostFavorite.setCancelFlag(Constants.CANCEL_FAVOR_FLAG);
            boolean updateRet = postFavoriteMapper.updateById(cancelPostFavorite) == 1;
            if (!updateRet) {
                throw new PetException("当前帖子取消收藏失败");
            }
            FavoriteDocument favoriteDocument =
                    favoriteRepository.findFavoriteDocumentByPostId(postFavorite.getPostId());
            if (favoriteDocument != null) {
                favoriteRepository.delete(favoriteDocument);
            }
        } else if (opFlag == 0) {
            // 收藏
            // 先判断是否已经存在取消收藏
            PostFavorite postFavorite = postFavoriteMapper.selectByUserIdAndPostId(userId, postId);
            PostFavorite favor = new PostFavorite();
            if (postFavorite != null && postFavorite.getCancelFlag().equals(Constants.CANCEL_FAVOR_FLAG)) {
                // 直接更新标记
                favor.setId(postFavorite.getId());
                favor.setCancelFlag(Constants.ON_FAVOR_FLAG);
                boolean updateRet = postFavoriteMapper.updateById(favor) == 1;
                if (!updateRet) {
                    throw new PetException("当前帖子更新收藏失败");
                }
            } else {
                // 插入新的收藏
                favor.setUserId(userId);
                favor.setPostId(postId);
                boolean insertRet = postFavoriteMapper.insert(favor) == 1;
                if (!insertRet) {
                    throw new PetException("当前帖子收藏失败");
                }
            }
            favoriteRepository.save(FavoriteDocument.builder()
                    .favorId(favor.getId()).favorUserId(userId).postId(postId)
                    .postUserId(post.getUserId()).postUserAvatar(user.getAvatarUrl())
                    .postUsername(user.getUsername())
                    .title(post.getTitle()).content(post.getContent())
                    .createTime(post.getCreateTime())
                    .updateTime(post.getUpdateTime())
                    .build());
        }

        // 获取帖子最新收藏数量
        return postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>().eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getCancelFlag, Constants.ON_FAVOR_FLAG));
    }

    @Transactional
    @Override
    public Boolean delete(Long userId, Long postId) {
        if (userId == null || postId == null) {
            throw new PetException("用户ID或者帖子ID错误，删除帖子失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (post == null || !post.getUserId().equals(userId)) {
            throw new PetException("当前用户不存在这个帖子，删除帖子失败");
        }
        if (post.getDeleteFlag().equals(Constants.DELETED_FLAG)) {
            throw new PetException("该帖子已经删除，无法二次删除");
        }

        Post deletedPost = new Post();
        deletedPost.setId(postId);
        deletedPost.setDeleteFlag(Constants.DELETED_FLAG);
        List<PostMedia> postMedia = postMediaMapper.selectMediaByPostId(postId);
        if (postMedia != null && !postMedia.isEmpty()) {
            List<Long> mediaDeleteIds = postMedia.stream().map(PostMedia::getId).toList();
            if (!mediaDeleteIds.isEmpty()) {
                int update = postMediaMapper.update(new LambdaUpdateWrapper<PostMedia>()
                        .in(PostMedia::getId, mediaDeleteIds)
                        .set(PostMedia::getDeleteFlag, Constants.DELETED_FLAG));
            }
        }

        // 删除帖子评论
        // 将对应帖子下的评论全部删除
        List<CommentSuper> commentSupers = commentSuperMapper.selectPostCommentsByObjectId(postId);
        List<Long> superDeletedIds = commentSupers.stream()
                .filter(commentSuper ->
                        commentSuper.getDeleteFlag().equals(Constants.NOT_DELETED_FLAG))
                .map(CommentSuper::getId).toList();

        int superDeleteRows = 0;
        if (!superDeletedIds.isEmpty()) {
            superDeleteRows = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                    .in(CommentSuper::getId, superDeletedIds)
                    .set(CommentSuper::getDeleteFlag, Constants.DELETED_FLAG));
        }

        if (superDeleteRows != superDeletedIds.size()) {
            throw new PetException("存在帖子评论删除失败");
        }

        // 同时删除评论对应的媒体（有的评论可能没有媒体）
        if (!superDeletedIds.isEmpty()) {
            List<CommentMedia> commentMedia = commentMediaMapper.selectList(new LambdaQueryWrapper<CommentMedia>()
                    .in(CommentMedia::getCommentId, superDeletedIds)
                    .eq(CommentMedia::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            List<Long> mediaDeleteIds = commentMedia.stream().map(CommentMedia::getId).toList();
            int mediaDeleteRows = 0;
            if (!mediaDeleteIds.isEmpty()) {
                mediaDeleteRows = commentMediaMapper.update(new LambdaUpdateWrapper<CommentMedia>()
                        .in(CommentMedia::getId, superDeletedIds)
                        .set(CommentMedia::getDeleteFlag, Constants.DELETED_FLAG));
            }

            if (mediaDeleteRows != mediaDeleteIds.size()) {
                throw new PetException("存在帖子媒体删除失败");
            }
        }

        // 如果帖子有话题，需要更新话题和帖子关联表中的数据
        List<TopicPost> topicPosts = topicPostMapper.selectTopicsByPostId(postId);
        if (!topicPosts.isEmpty()) {
            List<Long> topicDeleteIds = topicPosts.stream().map(TopicPost::getId).toList();
            int update = topicPostMapper.update(new LambdaUpdateWrapper<TopicPost>()
                    .in(TopicPost::getId, topicDeleteIds)
                    .set(TopicPost::getDeleteFlag, Constants.DELETED_FLAG));
        }

        Post deletePost = new Post();
        deletePost.setId(postId);
        deletePost.setDeleteFlag(Constants.DELETED_FLAG);
        boolean deleteRet = postMapper.updateById(deletePost) == 1;
        if (!deleteRet) {
            throw new PetException("帖子删除失败");
        }
        PostDocument postDocument = postRepository.findPostDocumentByPostId(postId);
        if (postDocument != null) {
            postRepository.delete(postDocument);
        }
        return true;
    }

    @Override
    public List<PostVO> getRecommendations(Long userId, Integer defaultTopN) {
        if (userId == null) {
            throw new PetException("用户信息错误，获取推荐失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 评分权重：收藏 > 点赞 > 点踩（负向）
        final double LIKE_SCORE = 2.0;
        final double FAVOR_SCORE = 3.0;
        final double DISLIKE_SCORE = -1.0;

        // 1. 查询所有有效点赞（未取消）
        LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(PostLike::getCancelFlag, Constants.ON_LIKE_DISLIKE_FLAG);
        List<PostLike> allLikes = postLikeMapper.selectList(likeWrapper);

        // 2. 查询所有有效收藏（未取消）
        LambdaQueryWrapper<PostFavorite> favorWrapper = new LambdaQueryWrapper<>();
        favorWrapper.eq(PostFavorite::getCancelFlag, Constants.ON_FAVOR_FLAG);
        List<PostFavorite> allFavors = postFavoriteMapper.selectList(favorWrapper);

        // 3. 查询所有有效点踩（未取消）
        LambdaQueryWrapper<PostDislike> dislikeWrapper = new LambdaQueryWrapper<>();
        dislikeWrapper.eq(PostDislike::getCancelFlag, Constants.ON_LIKE_DISLIKE_FLAG);
        List<PostDislike> allDislikes = postDislikeMapper.selectList(dislikeWrapper);

        // 4. 构建 用户-帖子 评分矩阵
        Map<Long, Map<Long, Double>> userPostMatrix = new HashMap<>();
        allLikes.forEach(l -> userPostMatrix
                .computeIfAbsent(l.getUserId(), k -> new HashMap<>())
                .merge(l.getPostId(), LIKE_SCORE, Double::sum));
        allFavors.forEach(f -> userPostMatrix
                .computeIfAbsent(f.getUserId(), k -> new HashMap<>())
                .merge(f.getPostId(), FAVOR_SCORE, Double::sum));
        allDislikes.forEach(d -> userPostMatrix
                .computeIfAbsent(d.getUserId(), k -> new HashMap<>())
                .merge(d.getPostId(), DISLIKE_SCORE, Double::sum));

        // 5. 新用户判定：矩阵中不存在该用户 → 返回 null，前端展示全部帖子
        if (!userPostMatrix.containsKey(userId)) {
            return null;
        }

        Map<Long, Double> targetVector = userPostMatrix.get(userId);

        // 6. 计算余弦相似度
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userPostMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;
            double sim = RecommendUtils.cosineSimilarity(targetVector, entry.getValue());
            if (sim > 0) similarityMap.put(otherUserId, sim);
        }

        // 7. 无相似用户 → 返回空列表
        if (similarityMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 8. 取 Top-K 相似用户
        List<Map.Entry<Long, Double>> topNeighbors = similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(Constants.TOP_K_NEIGHBORS)
                .toList();

        // 9. 加权聚合推荐分（排除目标用户已互动的帖子）
        Set<Long> interactedByTarget = targetVector.keySet();
        Map<Long, Double> recommendScores = new HashMap<>();
        for (Map.Entry<Long, Double> neighbor : topNeighbors) {
            double sim = neighbor.getValue();
            Map<Long, Double> neighborVector = userPostMatrix.get(neighbor.getKey());
            for (Map.Entry<Long, Double> postEntry : neighborVector.entrySet()) {
                Long postId = postEntry.getKey();
                if (interactedByTarget.contains(postId)) continue;
                // 只推荐邻居正向互动（score > 0）的帖子
                if (postEntry.getValue() > 0) {
                    recommendScores.merge(postId, sim * postEntry.getValue(), Double::sum);
                }
            }
        }

        if (recommendScores.isEmpty()) {
            return Collections.emptyList();
        }

        // 10. 按推荐分降序取 TopN 帖子ID
        List<Long> topPostIds = recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(defaultTopN)
                .map(Map.Entry::getKey)
                .toList();

        // 11. 查询帖子详情并组装 VO
        List<PostVO> result = new ArrayList<>();
        for (Long postId : topPostIds) {
            Post post = postMapper.selectById(postId);
            if (post == null || post.getDeleteFlag() == 1
                    || !post.getStatus().equals(Constants.POST_AUDIT_PASS)) continue;

            User user = userMapper.selectById(post.getUserId());
            if (user == null) continue;

            List<PostMedia> medias = postMediaMapper.selectMediaByPostId(postId);
            String mediaUrl = medias.isEmpty() ? "" : medias.get(0).getMediaUrl();

            result.add(PostVO.builder()
                    .postId(post.getId())
                    .userId(post.getUserId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .columnId(post.getColumnId())
                    .title(post.getTitle())
                    .likeCount(post.getLikeCount())
                    .mediaUrl(mediaUrl)
                    .build());
        }
        return result;
    }

    private List<String> insertPostMedia(Long postId, Integer mediaType, List<MultipartFile> files) {
        // 插入媒体
        if (files == null || files.isEmpty() ||
                postId == null || mediaType == null) {
            throw new PetException("上传的文件不合法");
        }

        // 上传的文件数量限制
        if (mediaType.equals(Constants.MEDIA_TYPE_VIDEO) &&
                files.size() > 1) {
            throw new PetException("视频文件最多上传一份");
        }
        if (mediaType.equals(Constants.MEDIA_TYPE_IMAGE) &&
                files.size() > 9) {
            throw new PetException("图片文件最多上传9份");
        }

        // 上传文件
        List<String> fileUrls = new ArrayList<>();
        List<PostMedia> postMedias = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = uploadFileUtil.uploadFileToOss(file);
            PostMedia postMedia = new PostMedia();
            postMedia.setPostId(postId);
            postMedia.setMediaUrl(fileUrl);
            postMedia.setMediaType(uploadFileUtil.getMediaType(file.getOriginalFilename()));
            postMedias.add(postMedia);
            fileUrls.add(fileUrl);
        }
        List<BatchResult> insert = postMediaMapper.insert(postMedias);

        return fileUrls;
    }

    public PostVO buildPostVOFromPost(Post post) {
        if (post == null) {
            return null;
        }

        Long postId = post.getId();
        Long userId = post.getUserId();

        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        String avatarUrl = user.getAvatarUrl();
        String username = user.getUsername();
        List<PostMedia> postMedias = postMediaMapper.selectMediaByPostId(postId);
        String firstMedia = postMedias.get(0).getMediaUrl();

        return PostVO.builder().postId(postId).columnId(post.getColumnId())
                .userId(userId).username(username).avatarUrl(avatarUrl)
                .title(post.getTitle()).likeCount(post.getLikeCount())
                .mediaUrl(firstMedia)
                .build();
    }

    public SelfPostVO buildSelfPostVOFromPostDocument(PostDocument postDocument) {
        if (postDocument == null) {
            return null;
        }

        Long postId = postDocument.getPostId();
        Long userId = postDocument.getUserId();
        Post post = postMapper.selectById(postId);
        List<PostMedia> postMedias = postMediaMapper.selectMediaByPostId(postId);
        Long commentCount = commentSuperMapper.selectCount(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(CommentSuper::getObjectId, postId)
                .eq(CommentSuper::getType, Constants.POST_COMMENT)
                .eq(CommentSuper::getStatus, Constants.COMMENT_AUDIT_PASS));
        String firstMedia = postMedias.get(0).getMediaUrl();

        return SelfPostVO.builder().postId(postId).userId(userId)
                .title(postDocument.getTitle()).content(postDocument.getContent())
                .commentCount(commentCount).likeCount(post.getLikeCount())
                .mediaUrl(firstMedia).status(postDocument.getStatus())
                .createTime(postDocument.getCreateTime())
                .build();
    }

    public PostDocument buildPostDocumentFromPost(Post post) {
        if (post == null) {
            return null;
        }

        Long postId = post.getId();
        Long userId = post.getUserId();
        List<PostMedia> postMedias = postMediaMapper.selectMediaByPostId(postId);

        String firstMedia = null;
        Integer mediaType = null;
        if (postMedias != null && !postMedias.isEmpty()) {
            firstMedia = postMedias.get(0).getMediaUrl();
            mediaType = postMedias.get(0).getMediaType();
        }

        return PostDocument.builder()
                .postId(postId).userId(userId)
                .title(post.getTitle()).content(post.getContent())
                .postMediaUrl(firstMedia).status(post.getStatus())
                .postMediaType(mediaType)
                .createTime(post.getCreateTime())
                .build();
    }

    public FavoriteDocument buildFavoriteDocumentFromPostFavorite(PostFavorite postFavorite) {
        if (postFavorite == null) {
            return null;
        }
        Long postId = postFavorite.getPostId();
        Post post = postMapper.selectById(postId);
        User postUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, post.getUserId()));
        String title = post.getTitle();
        String content = post.getContent();

        return FavoriteDocument.builder()
                .favorId(postFavorite.getId())
                .postId(postId).postUserId(postUser.getId()).postUsername(postUser.getUsername())
                .postUserAvatar(postUser.getAvatarUrl()).title(title).content(content)
                .favorUserId(postFavorite.getUserId())
                .createTime(postFavorite.getCreateTime())
                .updateTime(postFavorite.getUpdateTime())
                .build();
    }

    public FavoritePostVO buildFavoritePostVOFromFavoriteDocument(FavoriteDocument favoriteDocument) {
        if (favoriteDocument == null) {
            return null;
        }

        Long postId = favoriteDocument.getPostId();
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId));
        List<PostMedia> postMedia = postMediaMapper.selectMediaByPostId(postId);
        String mediaUrl = "";
        if (!postMedia.isEmpty() && post.getDeleteFlag().equals(Constants.NOT_DELETED_FLAG)) {
            mediaUrl = postMedia.get(0).getMediaUrl();
        }
        Long commentCount = commentSuperMapper.selectCount(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(CommentSuper::getObjectId, postId)
                .eq(CommentSuper::getType, Constants.POST_COMMENT));
        Long favorCount = postFavoriteMapper.selectCount(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getCancelFlag, Constants.ON_FAVOR_FLAG));

        return FavoritePostVO.builder()
                .postId(favoriteDocument.getPostId()).userId(favoriteDocument.getPostUserId())
                .username(favoriteDocument.getPostUsername())
                .avatar(favoriteDocument.getPostUserAvatar()).title(favoriteDocument.getTitle())
                .content(favoriteDocument.getContent()).commentCount(commentCount)
                .favorCount(favorCount).mediaUrl(mediaUrl)
                .likeCount(post.getLikeCount()).createTime(favoriteDocument.getUpdateTime())
                .build();
    }
}
