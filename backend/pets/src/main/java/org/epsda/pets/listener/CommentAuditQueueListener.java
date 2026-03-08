package org.epsda.pets.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.segment.mode.impl.SegmentModes;
import com.github.houbb.segment.support.segment.result.impl.SegmentResultHandlers;
import lombok.extern.slf4j.Slf4j;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.CommentSuperMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.CommentSuper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.message.CommentAuditInfo;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.third.TrieHolder;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 17:01
 *
 * @Author: 憨八嘎
 */
@Slf4j
@Component
@RabbitListener(queues = Constants.COMMENT_AUDIT_QUEUE)
public class CommentAuditQueueListener {

    @Autowired
    private CommentSuperMapper commentSuperMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TrieHolder trieHolder;
    @Autowired
    private MessagePushService messagePushService;

    @RabbitHandler
    public void commentAuditHandler(CommentAuditInfo commentAuditInfo) {
        Long commentId = commentAuditInfo.getCommentId();
        // 获取处于审核的评论的内容
        CommentSuper commentSuper = commentSuperMapper.selectOne(
                new LambdaQueryWrapper<CommentSuper>()
                .eq(CommentSuper::getId, commentId)
                .eq(CommentSuper::getStatus, Constants.COMMENT_ON_AUDIT)
                .eq(CommentSuper::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (commentSuper != null) {
            String text = commentSuper.getContent();
            // 先检查标题
            SegmentBs segmentBs = SegmentBs.newInstance();
            // 分词
            List<String> segments = segmentBs.segmentMode(SegmentModes.index())
                    .segment(text, SegmentResultHandlers.word());
            boolean auditRet = true;
            for (var segment : segments) {
                boolean search = trieHolder.trie.search(segment);
                if (search) {
                    log.warn("获取到的敏感词为：{}", segment);
                    auditRet = false;
                    break;
                }
            }

            boolean updateRet = true;
            CommonMessage commonMessage = new CommonMessage();
            commonMessage.setReceiverId(commentSuper.getUserId());
            commonMessage.setTitle(Constants.AUDIT_SYSTEM_MESSAGE_TITLE);
            commonMessage.setType(Constants.SYSTEM_MESSAGE);
            if (!auditRet) {
                // 未通过审核
                updateRet = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                        .eq(CommentSuper::getId, commentId)
                        .set(CommentSuper::getStatus, Constants.COMMENT_AUDIT_FAIL)) == 1;

                String content = "您的评论未通过审核，建议修改后重新提交。";
                commonMessage.setContent(content);
            } else {
                // 通过审核
                updateRet = commentSuperMapper.update(new LambdaUpdateWrapper<CommentSuper>()
                        .in(CommentSuper::getId, commentId)
                        .set(CommentSuper::getStatus, Constants.COMMENT_AUDIT_PASS)) == 1;
                String content = "您的评论通过审核，已对外可见。";
                commonMessage.setContent(content);
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
            }
            messagePushService.saveAndSendSystemMessage(commonMessage, Constants.AUDIT_SYSTEM_MESSAGE);
        }
    }
}
