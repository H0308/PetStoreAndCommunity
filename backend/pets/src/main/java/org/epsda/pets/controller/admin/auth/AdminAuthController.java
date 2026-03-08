package org.epsda.pets.controller.admin.auth;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.handler.ChatWebSocketHandler;
import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.service.AdminAuthService;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 21:34
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public ResultWrapper<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO) {
        return ResultWrapper.normal(adminAuthService.login(userLoginDTO));
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public ResultWrapper<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = null;
            try {
                userId = JwtUtil.extractUserId(token);
            } catch (Exception ignored) {
            }
            Date expiration = JwtUtil.extractExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(Constants.JWT_TOKEN_BLACK_LIST_PREFIX + token, "1", ttl, TimeUnit.MILLISECONDS);
            }
            chatWebSocketHandler.forceOffline(userId);
        }
        return ResultWrapper.normal(null);
    }
}
