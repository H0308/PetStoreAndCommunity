package org.epsda.pets.controller.user.auth;

import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.handler.ChatWebSocketHandler;
import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.dto.UserRegisterDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.service.AuthService;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @PostMapping("/login")
    public ResultWrapper<UserLoginVO> login(@Validated @RequestBody UserLoginDTO userLoginDTO) {
        return ResultWrapper.normal(authService.emailLogin(userLoginDTO));
    }

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
            // 强制用户下线
            chatWebSocketHandler.forceOffline(userId);
        }
        return ResultWrapper.normal(null);
    }

    @PostMapping("/register")
    public ResultWrapper<Boolean> register(@Validated @RequestBody UserRegisterDTO userRegisterDTO) {
        return ResultWrapper.normal(authService.register(userRegisterDTO));
    }

    // 获取Google OAuth2授权URL（前端调用后跳转授权弹窗）
    @GetMapping("/google/url")
    public ResultWrapper<String> getGoogleAuthUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";
        return ResultWrapper.normal(url);
    }

    // Google认证回调接口，授权后回调到当前接口并传递code参数
    // 该接口拿到code参数需要处理access_token以及拿到用户信息
    @GetMapping("/google/callback")
    public ResultWrapper<UserLoginVO> googleCallback(@RequestParam("code") String code) {
        return ResultWrapper.normal(authService.googleLogin(code));
    }
}
