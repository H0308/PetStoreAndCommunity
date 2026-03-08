package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.SneakyThrows;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.third.WebSocketSessionHolder;
import org.epsda.pets.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 18:50
 *
 * @Author: 憨八嘎
 */
@Service
public class MessagePushServiceImpl implements MessagePushService {

    @Autowired
    private WebSocketSessionHolder webSocketSessionHolder;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageMediaMapper messageMediaMapper;
    @Autowired
    private MessageLatestMapper messageLatestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotifyMapper notifyMapper;

    // 保存并发送系统消息
    @SneakyThrows
    public void saveAndSendSystemMessage(CommonMessage commonMessage, Integer systemMessageType) {
        Long receiverId = commonMessage.getReceiverId();
        String title = commonMessage.getTitle();
        String content = commonMessage.getContent();
        Notify notify = new Notify();
        notify.setReceiveUserId(receiverId);
        notify.setTitle(title);
        notify.setContent(content);
        notify.setType(systemMessageType);
        notifyMapper.insert(notify);
        this.sendMessage(commonMessage);
    }

    // 保存并发送交流消息
    @SneakyThrows
    public void saveAndSendChatMessage(CommonMessage commonMessage) {
        Long senderId = commonMessage.getSenderId();
        Long receiverId = commonMessage.getReceiverId();
        String content = commonMessage.getContent();
        Integer type = commonMessage.getType();
        String mediaUrl = commonMessage.getMediaUrl();
        Integer mediaType = commonMessage.getMediaType();
        Message message = new Message();
        message.setSendUserId(senderId);
        message.setReceiveUserId(receiverId);
        message.setType(type);
        message.setStatus(Constants.UNREAD_FLAG);
        if (StringUtils.hasText(content) && (type.equals(Constants.PRODUCT_MESSAGE) ||
                type.equals(Constants.ORDER_MESSAGE) || type.equals(Constants.CHAT_MESSAGE))) {
            message.setMessage(content);
        }
        // 保存消息
        messageMapper.insert(message);
        // 如果是媒体消息，则保存到媒体表中
        Long messageId = message.getId();
        if (messageId != null && type.equals(Constants.MEDIA_MESSAGE)) {
            MessageMedia messageMedia = new MessageMedia();
            messageMedia.setMessageId(messageId);
            messageMedia.setMediaUrl(mediaUrl);
            messageMedia.setMediaType(mediaType);
            messageMediaMapper.insert(messageMedia);
        }
        User sender = userMapper.selectById(senderId);
        // 需要更新最近消息表，消息表只有管理员能看到
        // 所以最近消息表只需要存储普通用户的ID即可
        // 消息发送者或者接受者都有可能为普通用户
        // 所以新增和查询时最新消息的发送者ID和接收者ID都有可能作为筛选ID
        // 最后最近消息表需要保证的是发送方字段一定得是普通用户ID，接收方字段一定得是管理员ID
        MessageLatest existedLatestMessage = messageLatestMapper.selectOne(new LambdaQueryWrapper<MessageLatest>()
                .eq(MessageLatest::getSendUserId, sender.getRoleId().equals(Constants.NORMAL_USER_FLAG) ? senderId : receiverId)
                .eq(MessageLatest::getReceiveUserId, sender.getRoleId().equals(Constants.ADMIN_FLAG) ? senderId : receiverId)
                .eq(MessageLatest::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (existedLatestMessage != null) {
            // 说明存在已有的消息记录，直接更新最新的消息
            messageLatestMapper.update(new LambdaUpdateWrapper<MessageLatest>()
                    .eq(MessageLatest::getSendUserId, sender.getRoleId().equals(Constants.NORMAL_USER_FLAG) ? senderId : receiverId)
                    .eq(MessageLatest::getReceiveUserId, sender.getRoleId().equals(Constants.ADMIN_FLAG) ? senderId : receiverId)
                    .set(MessageLatest::getMessageId, messageId));
        } else {
            // 不存在则直接插入
            MessageLatest messageLatest = new MessageLatest();
            messageLatest.setSendUserId(sender.getRoleId().equals(Constants.NORMAL_USER_FLAG) ? senderId : receiverId);
            messageLatest.setReceiveUserId(sender.getRoleId().equals(Constants.ADMIN_FLAG) ? senderId : receiverId);
            messageLatest.setMessageId(messageId);
            messageLatestMapper.insert(messageLatest);
        }

        this.sendMessage(commonMessage);
    }

    @SneakyThrows
    public void sendMessage(CommonMessage commonMessage) {
        Long receiverId = commonMessage.getReceiverId();
        WebSocketSession userSession = webSocketSessionHolder.getUserSession(receiverId);
        // 判断当前Session是否还有效，再发送数据
        if (userSession != null && userSession.isOpen()) {
            String json = JsonUtil.toJson(commonMessage);
            userSession.sendMessage(new TextMessage(json));
        }
    }
}
