package org.epsda.pets.third;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 10:32
 *
 * @Author: 憨八嘎
 */
@Data
@Component
public class WebSocketSessionHolder {
    private final ConcurrentHashMap<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    // 加入到Map中
    public boolean insertInto(Long userId, WebSocketSession socketSession) {
        if (userId == null || socketSession == null) {
            return false;
        }

        WebSocketSession prevSession = sessionMap.put(userId, socketSession);
        return true;
    }

    // 判断用户是否是否在Map中
    public boolean containUser(Long userId) {
        if (userId == null) {
            return false;
        }

        return sessionMap.containsKey(userId);
    }

    // 获取指定用户的Session
    public WebSocketSession getUserSession(Long userId) {
        if (userId == null) {
            return null;
        }

        return sessionMap.get(userId);
    }

    // 从Map中移除
    public boolean eraseFrom(Long userId) {
        if (userId == null) {
            return false;
        }

        WebSocketSession remove = sessionMap.remove(userId);
        return true;
    }
}
