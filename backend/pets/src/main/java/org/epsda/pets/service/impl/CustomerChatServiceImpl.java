package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.MessageMapper;
import org.epsda.pets.mapper.MessageMediaMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.dto.MessageListDTO;
import org.epsda.pets.pojo.vo.ChatMediaVO;
import org.epsda.pets.pojo.vo.CustomerServerInfo;
import org.epsda.pets.pojo.vo.MessageListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.CustomerChatService;
import org.epsda.pets.third.WebSocketSessionHolder;
import org.epsda.pets.utils.GenerateRandomIndices;
import org.epsda.pets.utils.OSSUploadFileUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 15:59
 *
 * @Author: 憨八嘎
 */
@Service
public class CustomerChatServiceImpl implements CustomerChatService {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageMediaMapper messageMediaMapper;
    @Autowired
    private WebSocketSessionHolder webSocketSessionHolder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OSSUploadFileUtil uploadFileUtil;

    @Override
    public PageVO<MessageListVO> list(MessageListDTO messageListDTO) {
        if (messageListDTO == null) {
            throw new PetException("消息信息错误，获取消息列表失败");
        }

        Long currentPage = messageListDTO.getCurrentPage();
        Long pageSize = messageListDTO.getPageSize();
        Long userId = messageListDTO.getUserId();
        Long serverId = messageListDTO.getServerId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Page<Message> messagePage = new Page<>(currentPage, pageSize);
        // 用户端只需要看到发送给自己的消息即可
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<Message>()
                .and(query -> query
                        .nested(q1 -> q1.eq(Message::getSendUserId, userId))
                        .or(q2 -> q2.eq(Message::getReceiveUserId, userId))
                )
                .ne(Message::getType, Constants.SYSTEM_MESSAGE)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .orderByDesc(Message::getCreateTime);

        Page<Message> messagePages = messageMapper.selectPage(messagePage, messageLambdaQueryWrapper);
        List<Message> records = messagePages.getRecords();
        List<MessageListVO> messageListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            records.forEach(record -> messageListVOS.add(this.buildMessageListVOFromMessage(record)));
        }

        return PageVO.<MessageListVO>builder()
                .currentPage(messagePages.getCurrent())
                .totalPages(messagePages.getPages())
                .totalCount(messagePages.getTotal())
                .totalRecords(messageListVOS)
                .build();
    }

    @Override
    public CustomerServerInfo getCustomerServerInfo(Long userId) {
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRoleId, Constants.ADMIN_FLAG)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (users.isEmpty()) {
            throw new PetException("获取客服失败");
        }

        // 筛选出所有在线的客服（但是在线客服不能是管理员自己）
        List<User> onlineUser = users.stream().filter(user ->
                !user.getId().equals(userId) &&
                webSocketSessionHolder.containUser(user.getId())).toList();
        if (onlineUser.isEmpty()){
            // 没有在线客服时，返回第一个管理员信息（离线状态），用户仍可发送离线消息
            User fallback = users.get(0);
            return CustomerServerInfo.builder()
                    .serverId(fallback.getId())
                    .serverName(fallback.getUsername())
                    .serverAvatarUrl(fallback.getAvatarUrl())
                    .onlineStatus(Constants.OFFLINE)
                    .build();
        }
        // 随机选择在线客服
        List<Integer> indices = new ArrayList<>();
        indices.add(0);
        if (onlineUser.size() > 1) {
            indices = GenerateRandomIndices.generateRandomIndices(onlineUser.size(), 1);
        }
        Long serverId = onlineUser.get(indices.get(0)).getId();
        String username = onlineUser.get(indices.get(0)).getUsername();
        String avatarUrl = onlineUser.get(indices.get(0)).getAvatarUrl();
        return CustomerServerInfo.builder()
                .serverId(serverId)
                .serverName(username)
                .serverAvatarUrl(avatarUrl)
                .onlineStatus(Constants.ONLINE)
                .build();
    }

    @Override
    public Boolean read(Long userId, Long sendUserId) {
        if (userId == null || sendUserId == null) {
            throw new PetException("用户信息错误，已读消息失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getSendUserId, sendUserId)
                .eq(Message::getReceiveUserId, userId)
                .ne(Message::getType, Constants.SYSTEM_MESSAGE)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG);
        List<Message> messages = messageMapper.selectList(messageLambdaQueryWrapper);
        if (!messages.isEmpty()) {
            messageMapper.update(new LambdaUpdateWrapper<Message>()
                    .set(Message::getStatus, Constants.READ_FLAG)
                    .in(Message::getId, messages.stream().map(Message::getId).toList()));
        }

        return true;
    }

    @Override
    public ChatMediaVO uploadMedia(Integer mediaType, MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null || mediaType == null) {
            throw new PetException("上传的文件不合法");
        }

        // 上传文件
        String fileUrl = uploadFileUtil.uploadFileToOss(file);

        return ChatMediaVO.builder()
                .mediaType(uploadFileUtil.getMediaType(file.getOriginalFilename()))
                .mediaUrl(fileUrl).build();
    }

    @Override
    public Boolean hasUnread(Long userId) {
        if (userId == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Long unreadCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiveUserId, userId)
                .eq(Message::getStatus, Constants.UNREAD_FLAG)
                .eq(Message::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        return unreadCount > 0;
    }

    public MessageListVO buildMessageListVOFromMessage(Message message) {
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
        MessageMedia messageMedia = messageMediaMapper.selectMessageMediaByMessageId(messageId);
        if (messageMedia != null) {
            // 说明存在文件
            messageListVO.setMediaUrl(messageMedia.getMediaUrl());
            messageListVO.setMediaType(messageMedia.getMediaType());
        }

        return messageListVO;
    }
}
