package org.epsda.pets.inteceptor;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 10:22
 *
 * @Author: 憨八嘎
 */
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter(Constants.TOKEN_HEADER);
            if (token != null && !token.isEmpty()) {
                // 去掉 Bearer 前缀拿到原始 token
                String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
                // 检查 token 是否在黑名单中（已登出）
                Boolean isBlacklisted = redisTemplate.hasKey("blacklist:token:" + rawToken);
                if (isBlacklisted) {
                    return false;
                }
                // 将token存入WebSocketSession属性
                attributes.put(Constants.TOKEN_HEADER, token);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}