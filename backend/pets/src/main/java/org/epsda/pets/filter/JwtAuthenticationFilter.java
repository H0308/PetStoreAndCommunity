package org.epsda.pets.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.epsda.pets.pojo.security.CustomUserDetails;
import org.epsda.pets.service.CustomUserDetailsService;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/20
 * Time: 22:20
 *
 * @Author: 憨八嘎
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 从请求头中获取Authorization字段，WebSocket握手时从query param获取
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null) {
            // WebSocket握手时token通过query param传递
            String paramToken = request.getParameter("Authorization");
            if (paramToken != null && !paramToken.isEmpty()) {
                authorizationHeader = paramToken;
            }
        }

        String jwt = null;
        Long userId = null;

        // 检查Authorization头是否存在且以"Bearer "开头
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // 移除Bearer 前缀
                jwt = authorizationHeader.substring(7);
                // 提取用户ID（不可变，邮箱/用户名可能被修改）
                userId = JwtUtil.extractUserId(jwt);
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": 401, \"message\": \"登录凭证已过期，请重新登录\"}");
                return;
            }
        }

        // 如果提取到用户ID且当前没有认证信息
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 通过userId加载用户详情（避免邮箱修改后查不到用户）
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            // 验证Token是否有效（使用userId校验，userId不可变）
            if (JwtUtil.validateToken(jwt, ((CustomUserDetails) userDetails).getUserId())) {

                // 检查token是否已被加入黑名单（登出后失效）
                Boolean isBlacklisted = redisTemplate.hasKey("blacklist:token:" + jwt);
                if (isBlacklisted) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 创建认证Token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 设置认证详情
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息设置到Security上下文中
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}
