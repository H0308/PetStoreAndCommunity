package org.epsda.pets.service.strategy;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.utils.JsonUtil;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.util.Map;

@Component("googleLogin")
public class GoogleLoginStrategy implements LoginStrategy {

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.client-secret}")
    private String clientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth2.proxy.host}")
    private String proxyHost;

    @Value("${google.oauth2.proxy.port}")
    private int proxyPort;

    @Autowired
    private UserMapper userMapper;

    // 创建带代理的WebClient，确保应用程序可以走VPN访问Google
    private WebClient createProxyWebClient() {
        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                        .type(ProxyProvider.Proxy.HTTP)
                        .host(proxyHost)
                        .port(proxyPort));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public UserLoginVO login(String code) {
        // 用认证回调的code换取access_token
        String accessToken = exchangeCodeForToken(code);

        // 用access_token获取Google用户信息
        Map<String, Object> googleUser = fetchGoogleUserInfo(accessToken);
        String email = (String) googleUser.get("email");

        if (email == null || email.isEmpty()) {
            throw new PetException("无法获取 Google 账号邮箱信息");
        }

        // 查找已注册用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("当前邮箱并没有绑定任何用户或当前用户不存在或者已经销户，登录失败。如果需要绑定邮箱，请使用可用邮箱登录后绑定Google邮箱");
        }

        String token = JwtUtil.generateToken(user.getId());
        return UserLoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roleId(user.getRoleId())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    // 用认证回调的code换取access_token
    private String exchangeCodeForToken(String code) {
        try {
            WebClient webClient = createProxyWebClient();
            String responseBody = webClient.post()
                    .uri(Constants.GOOGLE_EXCHANGE_ACCESS_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("code=" + code
                            + "&client_id=" + clientId
                            + "&client_secret=" + clientSecret
                            + "&redirect_uri=" + redirectUri
                            + "&grant_type=authorization_code")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> body = JsonUtil.toObject(responseBody, new TypeReference<Map<String, Object>>() {});
            String accessToken = (String) body.get("access_token");
            if (accessToken == null) {
                String error = (String) body.get("error_description");
                throw new PetException("Google 授权失败: " + (error != null ? error : "未知错误"));
            }
            return accessToken;
        } catch (PetException e) {
            throw e;
        } catch (Exception e) {
            throw new PetException("Google 授权码交换失败: " + e.getMessage());
        }
    }

    // 用access_token获取Google用户信息
    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        try {
            WebClient webClient = createProxyWebClient();
            String responseBody = webClient.get()
                    .uri(Constants.GOOGLE_USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return JsonUtil.toObject(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new PetException("获取 Google 用户信息失败: " + e.getMessage());
        }
    }
}
