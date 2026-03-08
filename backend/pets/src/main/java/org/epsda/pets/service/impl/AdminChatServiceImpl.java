package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.MessageLatestMapper;
import org.epsda.pets.mapper.MessageMapper;
import org.epsda.pets.mapper.MessageMediaMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Message;
import org.epsda.pets.pojo.MessageLatest;
import org.epsda.pets.pojo.MessageMedia;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.HistoryMessageDTO;
import org.epsda.pets.pojo.dto.LatestMessageDTO;
import org.epsda.pets.pojo.dto.MessageListDTO;
import org.epsda.pets.pojo.vo.*;
import org.epsda.pets.service.AdminChatService;
import org.epsda.pets.service.CustomerChatService;
import org.epsda.pets.third.WebSocketSessionHolder;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 17:32
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminChatServiceImpl implements AdminChatService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MessageLatestMapper messageLatestMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private CustomerChatService customerChatService;
    @Autowired
    private WebSocketSessionHolder webSocketSessionHolder;
    @Autowired
    private MessageMediaMapper messageMediaMapper;

    @Override
    public List<LatestMessageListVO> latest(LatestMessageDTO latestMessageDTO) {
        if (latestMessageDTO == null) {
            throw new PetException("用户信息错误，获取消息列表错误");
        }

        Long userId = latestMessageDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        // 构建查询
        String keyword = latestMessageDTO.getKeyword();
        List<Long> userIds = new ArrayList<>();
        userIds.add(-1L);
        if (StringUtils.hasText(keyword)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getUsername, keyword)
                    .ne(User::getId, userId)
                    .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                    .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            userIds = users.stream().map(User::getId).toList();
        }

        List<LatestMessageListVO> latestMessageListVOS = new ArrayList<>();
        List<MessageLatest> latestList =
                messageLatestMapper.selectList(new LambdaQueryWrapper<MessageLatest>()
                .ne(MessageLatest::getSendUserId, userId)
                .eq(MessageLatest::getReceiveUserId, userId)
                .eq(MessageLatest::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .in(StringUtils.hasText(keyword), MessageLatest::getSendUserId, userIds)
                .orderByDesc(MessageLatest::getUpdateTime));

        // 需要筛选出已经不存在的用户
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        List<Long> existedUserIds = users.stream().map(User::getId).toList();
        List<MessageLatest> existedReceiverMessages = latestList.stream().
                filter(messageLatest ->
                    existedUserIds.contains(messageLatest.getSendUserId()))
                .toList();

        // 构建出最近消息ID和用户对象的Map
        Map<Long, User> messageIdUserMap = new HashMap<>();
        List<Long> senderIds = existedReceiverMessages.stream().map(MessageLatest::getSendUserId).toList();
        List<Long> messageIds = existedReceiverMessages.stream().map(MessageLatest::getMessageId).toList();
        List<User> usersForMap = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, senderIds)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        // 构建用户ID和用户对象的Map
        Map<Long, User> userIdUserMap = new HashMap<>();
        for (User user : usersForMap) {
            userIdUserMap.put(user.getId(), user);
        }
        for (MessageLatest messageLatest : existedReceiverMessages) {
            messageIdUserMap.put(messageLatest.getId(), userIdUserMap.get(messageLatest.getSendUserId()));
        }
        // 构建出消息ID和未读消息个数的Map
        Map<Long, Long> unreadCountMap = new HashMap<>();
        List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getSendUserId, senderIds)
                .eq(Message::getReceiveUserId, userId)
                .eq(Message::getStatus, Constants.UNREAD_FLAG)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        // 此处需要注意，消息不一定是未读，构建时的最近消息对应的消息对象可能是已读
        for (Long messageId : messageIds) {
            unreadCountMap.put(messageId, 0L);
        }
        for (Message message : messages) {
            unreadCountMap.compute(message.getId(), (key, unreadCount) -> unreadCount + 1);
        }
        // 构建出消息ID和消息对象的Map
        // 注意需要额外构建Map，因为显示在消息列表的消息不一定是未读
        Map<Long, Message> idMessageMap = new HashMap<>();
        messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .in(Message::getId, messageIds)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        for (Message message : messages) {
            idMessageMap.put(message.getId(), message);
        }

        existedReceiverMessages.forEach(latest ->
                latestMessageListVOS.add(this.buildLatestMessageListVOFromMessageLatest(latest, messageIdUserMap, idMessageMap, unreadCountMap)));

        return latestMessageListVOS;
    }

    @Override
    public PageVO<MessageListVO> history(HistoryMessageDTO historyMessageDTO) {
        if (historyMessageDTO == null) {
            throw new PetException("消息信息错误，无法获取到历史消息");
        }

        Long currentPage = historyMessageDTO.getCurrentPage();
        Long pageSize = historyMessageDTO.getPageSize();
        Long userId = historyMessageDTO.getUserId();
        Long sendUserId = historyMessageDTO.getSendUserId();
        SecurityUtil.checkAdmin(userId);
        Page<Message> messagePage = new Page<>(currentPage, pageSize);
        // 管理员端需要看到指定用户和自己的消息记录，防止不同用户的消息冲突
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<Message>()
                .and(query -> query
                        .nested(q1 -> q1.eq(Message::getSendUserId, sendUserId).eq(Message::getReceiveUserId, userId))
                        .or(q2 -> q2.eq(Message::getSendUserId, userId).eq(Message::getReceiveUserId, sendUserId))
                )
                .ne(Message::getType, Constants.SYSTEM_MESSAGE)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .orderByDesc(Message::getCreateTime);

        Page<Message> messagePages = messageMapper.selectPage(messagePage, messageLambdaQueryWrapper);
        List<Message> records = messagePages.getRecords();
        List<MessageListVO> messageListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Long> messageIds = records.stream().map(Message::getId).toList();
            List<MessageMedia> messageMedia = messageMediaMapper.selectList(new LambdaQueryWrapper<MessageMedia>()
                    .in(MessageMedia::getMessageId, messageIds)
                    .eq(MessageMedia::getDeleteFlag, Constants.NOT_DELETED_FLAG));
            // 构建消息ID与媒体的映射表
            Map<Long, MessageMedia> messageIdMediaMap = new HashMap<>();
            for (MessageMedia media : messageMedia) {
                messageIdMediaMap.put(media.getMessageId(), media);
            }
            records.forEach(record -> messageListVOS.add(this.buildMessageListVOFromMessage(record, messageIdMediaMap)));
        }

        return PageVO.<MessageListVO>builder()
                .currentPage(messagePages.getCurrent())
                .totalPages(messagePages.getPages())
                .totalCount(messagePages.getTotal())
                .totalRecords(messageListVOS)
                .build();
    }

    @Override
    public Boolean read(Long userId, Long sendUserId) {
        if (userId == null || sendUserId == null) {
            throw new PetException("用户信息错误，读消息失败");
        }

        SecurityUtil.checkAdmin(userId);

        return customerChatService.read(userId, sendUserId);
    }

    @Override
    public ChatUserInfo getUserInfo(Long userId, Long sendUserId) {
        if (userId == null || sendUserId == null) {
            throw new PetException("用户信息错误，获取用户信息错误");
        }

        SecurityUtil.checkAdmin(userId);
        User user = userMapper.selectById(sendUserId);

        if (user == null) {
            throw new PetException("获取用户信息错误");
        }

        String username = user.getUsername();
        String avatarUrl = user.getAvatarUrl();
        // 判断是否在线
        boolean isOnline = webSocketSessionHolder.containUser(sendUserId);
        return ChatUserInfo.builder()
                .userId(sendUserId)
                .username(username)
                .avatar(avatarUrl)
                .onlineStatus(isOnline ? Constants.ONLINE : Constants.OFFLINE)
                .build();
    }

    private LatestMessageListVO buildLatestMessageListVOFromMessageLatest(MessageLatest messageLatest,
                                                                          Map<Long, User> userMap,
                                                                          Map<Long, Message> messageMap,
                                                                          Map<Long, Long> unreadCountMap) {
        if (messageLatest == null) {
            return null;
        }

        Long messageLatestId = messageLatest.getId();
        Long userId = messageLatest.getSendUserId();
        Long messageId = messageLatest.getMessageId();
        LocalDateTime updateTime = messageLatest.getUpdateTime();
        String content = messageMap.get(messageId).getMessage();
        Integer type = messageMap.get(messageId).getType();
        if (!StringUtils.hasText(content)) {
            if (type.equals(Constants.ORDER_MESSAGE)) {
                content = Constants.ORDER_MESSAGE_SHOWUP;
            } else if (type.equals(Constants.PRODUCT_MESSAGE)) {
                content = Constants.PRODUCT_MESSAGE_SHOWUP;
            } else if (type.equals(Constants.MEDIA_MESSAGE)) {
                content = Constants.MEDIA_MESSAGE_SHOWUP;
            }
        }

        // 判断用户是否在线
        boolean isOnline = webSocketSessionHolder.containUser(userId);

        return LatestMessageListVO.builder()
                .userId(userId).username(userMap.get(messageLatestId).getUsername())
                .content(content).latestTime(updateTime)
                .unreadCount(Math.toIntExact(unreadCountMap.get(messageId)))
                .onlineStatus(isOnline ? Constants.ONLINE : Constants.OFFLINE)
                .roleId(userMap.get(messageLatestId).getRoleId())
                .avatar(userMap.get(messageLatestId).getAvatarUrl()).build();
    }

    public MessageListVO buildMessageListVOFromMessage(Message message, Map<Long, MessageMedia> messageIdMediaMap) {
        if (message == null) {
            return null;
        }

        Long messageId = message.getId();
        Long sendUserId = message.getSendUserId();
        Long receiveUserId = message.getReceiveUserId();
        String content = message.getMessage();
        Integer type = message.getType();
        MessageListVO messageListVO = MessageListVO.builder()
                .messageId(messageId).sendUserId(sendUserId)
                .receiveUserId(receiveUserId).content(content)
                .type(type).createTime(message.getCreateTime()).build();
        MessageMedia messageMedia = messageIdMediaMap.get(messageId);
        if (messageMedia != null) {
            // 说明存在文件
            messageListVO.setMediaUrl(messageMedia.getMediaUrl());
            messageListVO.setMediaType(messageMedia.getMediaType());
        }

        return messageListVO;
    }
}
