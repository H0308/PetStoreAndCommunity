package org.epsda.pets.config;

import org.epsda.pets.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.DispatcherType;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/20
 * Time: 22:18
 *
 * @Author: 憨八嘎
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // BCrypt算法加密用户密码
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护（因为使用JWT）
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        // 允许异步分发（SSE/流式响应需要）
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        // 允许以下接口无需验证，包括Knife4j入口
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**", "/swagger-resources/**",
                                "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/favicon.ico",
                                "/api/user/auth/login",
                                "/api/user/auth/register",
                                "/api/user/auth/google/url",
                                "/api/user/auth/google/callback",
                                "/api/user/captcha/getCaptcha",
                                "/api/user/captcha/getEmailCaptcha",
                                "/api/user/captcha/checkEmailCaptcha",
                                "/api/user/captcha/check",
                                "/api/admin/auth/login",
                                "/api/user/index/getCarousels",
                                "/api/user/index/getLatestProducts",
                                "/api/user/index/getSuperCategories",
                                "/api/user/index/getSubCategories",
                                "/api/user/post/list",
                                "/api/user/post/column").permitAll()
                        // 管理员接口需要管理员权限
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 用户接口需要普通用户或管理员权限
                        .requestMatchers("/api/user/**").hasAnyRole("NORMAL", "ADMIN")
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
