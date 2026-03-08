package org.epsda.pets.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.mapper.MessageLatestMapper;
import org.epsda.pets.mapper.MessageMapper;
import org.epsda.pets.mapper.MessageMediaMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Message;
import org.epsda.pets.pojo.MessageLatest;
import org.epsda.pets.pojo.MessageMedia;
import org.epsda.pets.pojo.User;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.third.WebSocketSessionHolder;
import org.epsda.pets.utils.JsonUtil;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 10:23
 *
 * @Author: 憨八嘎
 */
@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private WebSocketSessionHolder webSocketSessionHolder;
    @Autowired
    private MessagePushService messagePushService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = this.getUserIdFromHeader(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户凭证有误"));
            return;
        }
        // 判断用户是否已经在线
        if (webSocketSessionHolder.containUser(userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("当前用户已建立连接，请勿重复建立"));
            return;
        }

        // 将用户加入到Session管理
        webSocketSessionHolder.insertInto(userId, session);
        // 通知所有在线用户更改指定用户的在线状态
        broadCastChangeOnlineStatus(userId, Constants.ONLINE);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 接收到消息根据消息类型进行消息处理
        if (message instanceof TextMessage) {
            CommonMessage commonMessage = JsonUtil.toObject((String) message.getPayload(), CommonMessage.class);
            if (commonMessage == null) {
                return;
            }
            String content = commonMessage.getContent();
            if (content.length() > 2000) {
                return;
            }
            Integer type = commonMessage.getType();
            if (type.equals(Constants.SYSTEM_MESSAGE)) {
                String title = commonMessage.getTitle();
                if (!StringUtils.hasText(title) || title.length() > 50) {
                    return;
                }
                // 处理系统消息
                this.handleSystemMessages(session, commonMessage);
            } else if (type.equals(Constants.CHAT_MESSAGE) ||
                type.equals(Constants.ORDER_MESSAGE) ||
                type.equals(Constants.PRODUCT_MESSAGE) ||
                type.equals(Constants.MEDIA_MESSAGE)) {
                // 处理聊天消息
                this.handleCommunicationMessages(session, commonMessage);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        exception.printStackTrace();
        this.removeUserFromSessionsHolder(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        this.removeUserFromSessionsHolder(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @SneakyThrows
    public void handleSystemMessages(WebSocketSession session, CommonMessage commonMessage) {
        // 当前系统通知只会由系统发送，所有用户一概不允许主动发送系统消息
        log.warn("非法消息类型：用户[{}]尝试发送系统消息，已拦截", commonMessage.getSenderId());
    }

    @SneakyThrows
    public void handleCommunicationMessages(WebSocketSession session, CommonMessage commonMessage) {
        messagePushService.saveAndSendChatMessage(commonMessage);
    }

    @SneakyThrows
    public void broadCastChangeOnlineStatus(Long userId, Integer onlineStatus) {
        webSocketSessionHolder.getSessionMap().entrySet().forEach(entry -> {
            Long receiverId = entry.getKey();
            CommonMessage commonMessage = new CommonMessage();
            commonMessage.setSenderId(userId);
            commonMessage.setReceiverId(receiverId);  // 修改为接收者的ID
            commonMessage.setType(Constants.STATUS_MESSAGE);
            commonMessage.setOnlineStatus(onlineStatus);
            String json = JsonUtil.toJson(commonMessage);
            WebSocketSession userSession = webSocketSessionHolder.getUserSession(receiverId);
            try {
                if (userSession != null && userSession.isOpen()) {
                    userSession.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                throw new PetException("在线状态更新消息发送失败");
            }
        });
    }

    public void removeUserFromSessionsHolder(WebSocketSession session) {
        // 需要将指定用户从Session管理中移除
        // 先从Header中拿到userId
        Long userId = this.getUserIdFromHeader(session);
        if (userId != null && webSocketSessionHolder.containUser(userId)) {
            // 需要判断当前用户的Session与实际参数的Session是否是同一个再执行关闭
            // 否则可能会因为用户多开导致把旧的Session移除了
            WebSocketSession userSession = webSocketSessionHolder.getUserSession(userId);
            if (userSession == session) {
                // 此时可以删除
                webSocketSessionHolder.eraseFrom(userId);
                // 通知所有在线用户离线消息
                broadCastChangeOnlineStatus(userId, Constants.OFFLINE);
            }
        }
    }

    // 强制用户登出
    public void forceOffline(Long userId) {
        if (userId == null) {
            return;
        }
        WebSocketSession userSession = webSocketSessionHolder.getUserSession(userId);
        if (userSession == null) {
            return;
        }
        webSocketSessionHolder.eraseFrom(userId);
        broadCastChangeOnlineStatus(userId, Constants.OFFLINE);
        if (userSession.isOpen()) {
            try {
                userSession.close(CloseStatus.NORMAL.withReason("logout"));
            } catch (IOException e) {
                log.warn("用户下线失败");
            }
        }
    }

    // 从Header中获取UserId
    public Long getUserIdFromHeader(WebSocketSession session) {
        // 先从token中拿到userId数据
        String header = (String) session.getAttributes().get(Constants.TOKEN_HEADER);
        if (header == null) {
            return null;
        }
        String token = "";
        if (header.startsWith(Constants.TOKEN_START_FLAG)) {
            token = header.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        Long userId = null;
        try {
            // 提取邮箱
            userId = JwtUtil.extractUserId(token);
        } catch (ExpiredJwtException e) {
            log.info("登录凭证已过期");
        }
        return userId;
    }
}
