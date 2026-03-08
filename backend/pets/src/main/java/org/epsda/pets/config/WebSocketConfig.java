package org.epsda.pets.config;

import org.epsda.pets.handler.ChatWebSocketHandler;
import org.epsda.pets.inteceptor.AuthHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 10:09
 *
 * @Author: 憨八嘎
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/api/customer-chat")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
