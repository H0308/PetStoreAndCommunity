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
import org.epsda.pets.mapper.PostMapper;
import org.epsda.pets.pojo.Post;
import org.epsda.pets.pojo.document.PostDocument;
import org.epsda.pets.pojo.message.PostAuditInfo;
import org.epsda.pets.repository.PostRepository;
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
 * Time: 16:17
 *
 * @Author: 憨八嘎
 */
@Slf4j
@Component
@RabbitListener(queues = Constants.POST_AUDIT_QUEUE)
public class PostAuditQueueListener {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private TrieHolder trieHolder;
    @Autowired
    private MessagePushService messagePushService;
    @Autowired
    private PostRepository postRepository;

    @RabbitHandler
    public void postAuditHandler(PostAuditInfo postAuditInfo) {
        Long postId = postAuditInfo.getPostId();
        // 获取处于审核的帖子的内容
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getStatus, Constants.POST_ON_AUDIT)
                .eq(Post::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (post != null) {
            // 拼接内容
            String text = post.getTitle() + " " + post.getContent();
            SegmentBs segmentBs = SegmentBs.newInstance();
            // 分词
            List<String> segments = segmentBs.segmentMode(SegmentModes.index())
                    .segment(text, SegmentResultHandlers.word());
            boolean auditResult = true;
            for (var segment : segments) {
                boolean search = trieHolder.trie.search(segment);
                if (search) {
                    auditResult = false;
                    log.warn("获取到的敏感词为：{}", segment);
                    break;
                }
            }

            boolean updateRet = true;
            PostDocument postDocument = postRepository.findPostDocumentByPostId(postId);
            CommonMessage commonMessage = new CommonMessage();
            commonMessage.setReceiverId(post.getUserId());
            commonMessage.setTitle(Constants.AUDIT_SYSTEM_MESSAGE_TITLE);
            commonMessage.setType(Constants.SYSTEM_MESSAGE);
            if (!auditResult) {
                // 未通过审核
                updateRet = postMapper.update(new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .set(Post::getStatus, Constants.POST_AUDIT_FAIL)) == 1;
                postDocument.setStatus(Constants.POST_AUDIT_FAIL);
                String content = "您的帖子" + "《" + post.getTitle() + "》" + "未通过审核，建议修改后重新提交。";
                commonMessage.setContent(content);
            } else {
                // 通过审核
                updateRet = postMapper.update(new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .set(Post::getStatus, Constants.POST_AUDIT_PASS)) == 1;
                postDocument.setStatus(Constants.POST_AUDIT_PASS);

                String content = "您的帖子" + "《" + post.getTitle() + "》" + "通过审核，已对外可见。";
                commonMessage.setContent(content);
            }
            messagePushService.saveAndSendSystemMessage(commonMessage, Constants.AUDIT_SYSTEM_MESSAGE);
            postRepository.save(postDocument);
        }
    }
}
