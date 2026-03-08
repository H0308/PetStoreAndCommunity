package org.epsda.pets.utils;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.pojo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/29
 * Time: 18:53
 *
 * @Author: 憨八嘎
 */
public class SecurityUtil {

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    // 生成随机八位密码（包含大小写字母、数字、特殊字符）
    public static String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
    // 判断是否是管理员
    public static void checkAdmin(Long userId) {
        if (userId == null) {
            throw new PetException("用户信息错误，校验失败");
        }
        // 水平权限校验
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long securityUserId = principal.getUserId();
        if (!securityUserId.equals(userId)) {
            throw new PetException("不允许访问非当前登录用户的资源");
        }
        // 垂直权限校验
        if (!getRoleIdFromPrinciple().equals(Constants.ADMIN_FLAG)) {
            throw new PetException("用户不存在或者用户并非管理员");
        }
    }

    // 防止水平越权
    public static void checkHorizontalOversteppedForNormal(Long frontendUserId) {
        if (frontendUserId == null) {
            throw new PetException("用户信息错误，不允许访问当前资源");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long securityUserId = principal.getUserId();
        // 只对普通用户进行校验
        if (Constants.NORMAL_USER_FLAG.equals(getRoleIdFromPrinciple()) &&
                !securityUserId.equals(frontendUserId)) {
            throw new PetException("不允许访问非当前登录用户的资源");
        }
    }

    // 获取当前用户角色ID
    public static Long getRoleIdFromPrinciple() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getRoleId();
    }
}
