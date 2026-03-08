package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.executor.BatchResult;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.ProductCommentBO;
import org.epsda.pets.pojo.dto.BaseCommentTextDTO;
import org.epsda.pets.pojo.dto.ProductCommentTextDTO;
import org.epsda.pets.pojo.message.CommentAuditInfo;
import org.epsda.pets.pojo.vo.BaseCommentVO;
import org.epsda.pets.pojo.vo.CommentMediaVO;
import org.epsda.pets.pojo.vo.CommentTextVO;
import org.epsda.pets.pojo.vo.ProductCommentVO;
import org.epsda.pets.service.CommentService;
import org.epsda.pets.utils.OSSUploadFileUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 20:25
 *
 * @Author: 憨八嘎
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private CommentSuperMapper commentSuperMapper;
    @Autowired
    private ProductCommentSubMapper productCommentSubMapper;
    @Autowired
    private CommentMediaMapper commentMediaMapper;
    @Autowired
    private OSSUploadFileUtil uploadFileUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<ProductCommentVO> getProductComment(Long productId) {
        List<ProductCommentBO> productCommentBOS = commentSuperMapper.selectProductCommentsByObjectId(productId);
        if (productCommentBOS == null || productCommentBOS.isEmpty()) {
            return null;
        }

        List<ProductCommentVO> productCommentVOS = new ArrayList<>();
        productCommentBOS.forEach(comment -> {
            ProductCommentVO productCommentVO =
                    new ProductCommentVO(this.getBaseComment(comment.getCommentSuper()));
            // 已删除的评论可能没有子表记录
            if (comment.getProductCommentSub() != null) {
                productCommentVO.setStars(comment.getProductCommentSub().getStars());
            }
            productCommentVOS.add(productCommentVO);
        });

        return productCommentVOS;
    }

    @Transactional
    @Override
    public CommentTextVO postProductComment(ProductCommentTextDTO productCommentTextDTO, List<MultipartFile> files) {
        // 判断是否是合规的评论
        if (productCommentTextDTO == null || productCommentTextDTO.getObjectId() == null ||
            productCommentTextDTO.getUserId() == null || productCommentTextDTO.getContent() == null ||
            productCommentTextDTO.getContent().trim().isEmpty()) {
            throw new PetException("当前评论非法");
        }
        Long userId = productCommentTextDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(User::getBanFlag, Constants.NOT_BANNED_FLAG));
        if (user == null) {
            throw new PetException("当前用户不存在或者用户已经被禁言，创建评论失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        if (!StringUtils.hasText(user.getRealName()) || !StringUtils.hasText(user.getIdCard())) {
            throw new PetException("用户未进行实名认证，无法发布商品评论");
        }
        ProductSuper productSuper = productSuperMapper.selectOne(new LambdaQueryWrapper<ProductSuper>()
                        .eq(ProductSuper::getId, productCommentTextDTO.getObjectId())
                        .eq(ProductSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                        .and(productSuperQueryWrapper -> productSuperQueryWrapper
                                .eq(ProductSuper::getStatus, Constants.ON_SELLING)
                                .or()
                                .eq(ProductSuper::getStatus, Constants.SOLD_OUT)));
        if (productSuper == null) {
            throw new PetException("当前商品不存在，创建评论失败");
        }

        CommentSuper commentSuper = new CommentSuper();
        commentSuper.setObjectId(productCommentTextDTO.getObjectId());
        commentSuper.setUserId(productCommentTextDTO.getUserId());
        commentSuper.setType(Constants.PRODUCT_COMMENT); // 设置商品评论
        Long parentId = productCommentTextDTO.getParentId();
        // 如果存在内容完全相同的评论，本系统不处理，新增内容一样的评论不会影响同样内容但是已删除的评论
        if (parentId != null && parentId != 0) {
            CommentSuper parentComment = commentSuperMapper.selectById(parentId);
            if (parentComment == null ||
                    !parentComment.getObjectId().equals(productCommentTextDTO.getObjectId())) {
                throw new PetException("父级评论不存在或者父级评论指向商品与当前评论指向商品不符，创建评论失败");
            }
            commentSuper.setParentId(parentId);
        }
        commentSuper.setContent(productCommentTextDTO.getContent());
        ProductCommentSub productCommentSub = new ProductCommentSub();
        // 管理员和普通用户回复消息可以不用填写星星
        if (user.getRoleId().equals(Constants.ADMIN_FLAG) || (parentId != null && parentId != 0)) {
            boolean ret = commentSuperMapper.insert(commentSuper) == 1;
            Long commentId = 0L;
            if (ret) {
                commentId = commentSuper.getId();
            }
            // 插入媒体
            boolean mediaInsertRet = insertCommentMedia(commentId, files);
            // 调用RabbitMQ进行评论审核
            CommentAuditInfo commentAuditInfo = new CommentAuditInfo(commentId);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(Constants.AUDIT_EXCHANGE, Constants.COMMENT_AUDIT_ROUTING_KEY, commentAuditInfo);
                }
            });
            return CommentTextVO.builder()
                    .commentId(commentSuper.getId())
                    .successFlag(ret)
                    .build();
        }

        // 普通用户需要填写评价星星数量，回复不需要填星星，上面逻辑已经处理
        boolean insertRet = commentSuperMapper.insert(commentSuper) == 1;
        boolean subInsertRet = true;
        Long commentId = 0L;
        if (insertRet) {
            // 拿到主键ID
            commentId = commentSuper.getId();
        }
        if (parentId == null) {
            productCommentSub.setCommentId(commentId);
            productCommentSub.setStars(productCommentTextDTO.getStars());
            subInsertRet = productCommentSubMapper.insert(productCommentSub) == 1;
        }
        // 插入媒体
        boolean mediaInsertRet = insertCommentMedia(commentId, files);
        // 调用RabbitMQ进行评论审核
        CommentAuditInfo commentAuditInfo = new CommentAuditInfo(commentSuper.getId());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(Constants.AUDIT_EXCHANGE, Constants.COMMENT_AUDIT_ROUTING_KEY, commentAuditInfo);
            }
        });
        return CommentTextVO.builder()
                .commentId(commentSuper.getId())
                .successFlag(insertRet && subInsertRet)
                .build();
    }

    @Transactional
    @Override
    public Boolean deleteProductComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new PetException("评论信息错误，删除评论失败");
        }
        CommentSuper commentSuper = commentSuperMapper.selectOne(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getId, commentId)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (commentSuper == null) {
            throw new PetException("商品评论ID指定的评论不存在或者已经被删除，无法删除");
        }

        if (!commentSuper.getUserId().equals(userId)) {
            throw new PetException("当前商品评论并不是指定用户ID的评论");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        if (user.getRoleId().equals(Constants.ADMIN_FLAG)) {
            // 管理员只更新评论总表和媒体表
            CommentSuper adminCommentSuper = new CommentSuper();
            adminCommentSuper.setId(commentId);
            adminCommentSuper.setDeleteFlag(Constants.DELETED_FLAG);
            List<CommentMedia> commentMedia = commentMediaMapper.selectMediaByCommentId(commentId);
            if (commentMedia != null && !commentMedia.isEmpty()) {
                commentMedia.forEach(media -> {
                    CommentMedia deleteCommentMedia = new CommentMedia();
                    deleteCommentMedia.setId(media.getId());
                    deleteCommentMedia.setDeleteFlag(Constants.DELETED_FLAG);
                    boolean updateRet = commentMediaMapper.updateById(deleteCommentMedia) == 1;
                    if (!updateRet) {
                        throw new PetException("评论媒体删除失败，评论无法删除");
                    }
                });
            }
            return commentSuperMapper.updateById(adminCommentSuper) == 1;
        }

        CommentSuper normalCommentSuper = new CommentSuper();
        normalCommentSuper.setId(commentId);
        normalCommentSuper.setDeleteFlag(Constants.DELETED_FLAG);

        // 删除媒体
        List<CommentMedia> commentMedia = commentMediaMapper.selectMediaByCommentId(commentId);
        if (commentMedia != null && !commentMedia.isEmpty()) {
            commentMedia.forEach(media -> {
                CommentMedia deleteCommentMedia = new CommentMedia();
                deleteCommentMedia.setId(media.getId());
                deleteCommentMedia.setDeleteFlag(Constants.DELETED_FLAG);
                boolean updateRet = commentMediaMapper.updateById(deleteCommentMedia) == 1;
                if (!updateRet) {
                    throw new PetException("评论媒体删除失败，评论无法删除");
                }
            });
        }

        boolean updateRet = commentSuperMapper.updateById(normalCommentSuper) == 1 && productCommentSubMapper.deleteByCommentId(commentId);
        if (!updateRet) {
            throw new PetException("商品评论删除失败");
        }

        return true;
    }

    @Override
    public List<BaseCommentVO> getPostComment(Long postId) {
        List<CommentSuper> commentSupers = commentSuperMapper.selectPostCommentsByObjectId(postId);
        if (commentSupers == null || commentSupers.isEmpty()) {
            return null;
        }

        List<BaseCommentVO> baseCommentVOS = new ArrayList<>();
        commentSupers.forEach(commentSuper -> baseCommentVOS.add(this.getBaseComment(commentSuper)));

        return baseCommentVOS;
    }

    @Transactional
    @Override
    public CommentTextVO postPostComment(BaseCommentTextDTO baseCommentTextDTO, List<MultipartFile> files) {
        // 判断是否是合规的评论
        if (baseCommentTextDTO == null || baseCommentTextDTO.getObjectId() == null ||
                baseCommentTextDTO.getUserId() == null || baseCommentTextDTO.getContent() == null ||
                baseCommentTextDTO.getContent().trim().isEmpty()) {
            throw new PetException("当前评论非法");
        }
        Long userId = baseCommentTextDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .eq(User::getBanFlag, Constants.NOT_BANNED_FLAG));
        if (user == null) {
            throw new PetException("当前用户不存在或者已经被禁言，创建评论失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        if (!StringUtils.hasText(user.getRealName()) || !StringUtils.hasText(user.getIdCard())) {
            throw new PetException("用户未进行实名认证，无法发布帖子评论");
        }
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, baseCommentTextDTO.getObjectId())
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (post == null || !post.getStatus().equals(Constants.POST_AUDIT_PASS)) {
            throw new PetException("当前帖子不存在或者帖子状态错误，创建评论失败");
        }

        CommentSuper commentSuper = new CommentSuper();
        commentSuper.setObjectId(baseCommentTextDTO.getObjectId());
        commentSuper.setUserId(baseCommentTextDTO.getUserId());
        commentSuper.setType(Constants.POST_COMMENT); // 设置帖子评论
        Long parentId = baseCommentTextDTO.getParentId();
        // 如果存在内容完全相同的评论，本系统不处理，新增内容一样的评论不会影响同样内容但是已删除的评论
        if (parentId != null && parentId != 0) {
            CommentSuper parentComment = commentSuperMapper.selectById(parentId);
            if (parentComment == null ||
                    !parentComment.getObjectId().equals(baseCommentTextDTO.getObjectId())) {
                throw new PetException("父级评论不存在或者父级评论指向帖子与当前评论指向的帖子不符，创建评论失败");
            }
            commentSuper.setParentId(parentId);
        }
        commentSuper.setContent(baseCommentTextDTO.getContent());

        boolean ret = commentSuperMapper.insert(commentSuper) == 1;
        if (!ret) {
            throw new PetException("评论发布失败");
        }

        // 插入媒体
        boolean mediaInsertRet = this.insertCommentMedia(commentSuper.getId(), files);

        // 调用RabbitMQ进行评论审核
        CommentAuditInfo commentAuditInfo = new CommentAuditInfo(commentSuper.getId());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(Constants.AUDIT_EXCHANGE, Constants.COMMENT_AUDIT_ROUTING_KEY, commentAuditInfo);
            }
        });

        return CommentTextVO.builder()
                .commentId(commentSuper.getId())
                .successFlag(true)
                .build();
    }

    @Transactional
    @Override
    public Boolean deletePostComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new PetException("评论信息错误，删除评论失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        CommentSuper commentSuper = commentSuperMapper.selectOne(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getId, commentId)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (commentSuper == null) {
            throw new PetException("商品评论ID指定的评论不存在或已经被删除，无法删除");
        }

        if (!commentSuper.getUserId().equals(userId)) {
            throw new PetException("当前商品评论并不是指定用户ID的评论");
        }

        CommentSuper deletedCommentSuper = new CommentSuper();
        deletedCommentSuper.setId(commentId);
        deletedCommentSuper.setDeleteFlag(Constants.DELETED_FLAG);
        List<CommentMedia> commentMedia = commentMediaMapper.selectMediaByCommentId(commentId);
        if (commentMedia != null && !commentMedia.isEmpty()) {
            commentMedia.forEach(media -> {
                CommentMedia deleteCommentMedia = new CommentMedia();
                deleteCommentMedia.setId(media.getId());
                deleteCommentMedia.setDeleteFlag(Constants.DELETED_FLAG);
                boolean updateRet = commentMediaMapper.updateById(deleteCommentMedia) == 1;
                if (!updateRet) {
                    throw new PetException("评论媒体删除失败，评论无法删除");
                }
            });
        }
        boolean updateRet = commentSuperMapper.updateById(deletedCommentSuper) == 1;
        if (!updateRet) {
            throw new PetException("帖子评论删除失败");
        }

        return true;
    }

    private boolean insertCommentMedia(Long commentId, List<MultipartFile> files) {
        if (files == null || files.isEmpty() ||
                commentId == null) {
            throw new PetException("上传的文件不合法");
        }

        if (files.size() > 9) {
            throw new PetException("文件上传个数最大为9");
        }

        CommentSuper commentSuper = commentSuperMapper.selectOne(new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getId, commentId)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (commentSuper == null) {
            throw new PetException("当前评论ID对应的评论不存在");
        }

        // 上传文件
        List<CommentMedia> commentMedias = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = uploadFileUtil.uploadFileToOss(file);
            CommentMedia commentMedia = new CommentMedia();
            commentMedia.setCommentId(commentId);
            commentMedia.setMediaUrl(fileUrl);
            commentMedia.setMediaType(uploadFileUtil.getMediaType(file.getOriginalFilename()));
            commentMedias.add(commentMedia);
        }

        List<BatchResult> insert = commentMediaMapper.insert(commentMedias);

        return true;
    }

    public BaseCommentVO getBaseComment(CommentSuper commentSuper) {
        // 根据用户ID获取到用户名
        // 此处不考虑用户状态，因为尽管用户可能已经销户或者被删除了，但是因为发布过评论，所以还是正常显示当前用户
        User user = userMapper.selectById(commentSuper.getUserId());
        Long commentId = commentSuper.getId();
        List<CommentMedia> commentMedia = commentMediaMapper.selectMediaByCommentId(commentId);
        List<CommentMediaVO> commentMediaVOS = new ArrayList<>();
        commentMedia.forEach(media -> commentMediaVOS.add(CommentMediaVO.builder()
                .mediaType(media.getMediaType())
                .mediaUrl(media.getMediaUrl())
                .build()));
        return BaseCommentVO.builder()
                .commentId(commentId).userId(user.getId())
                .username(user.getUsername()).avatarUrl(user.getAvatarUrl())
                .parentId(commentSuper.getParentId()).content(commentSuper.getContent())
                .status(commentSuper.getStatus()).deleteFlag(commentSuper.getDeleteFlag())
                .mediaVOS(commentMediaVOS).updateTime(commentSuper.getUpdateTime())
                .build();
    }
}
