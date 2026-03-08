package org.epsda.pets.utils;

import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * Description: 验证码生成工具类
 * User: 18483
 * Date: 2026/02/22
 *
 * @Author: 憨八嘎
 */
public class CaptchaUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    // 字母+数字混合验证码字符集（去除易混淆字符 0/O/1/I/l）
    private static final String ALPHANUMERIC_CHARS =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    // 数字+字母混合
    public static String generateAlphanumericCaptcha(int length) {
        return generate(ALPHANUMERIC_CHARS, length);
    }

    private static String generate(String chars, int length) {
        if (length <= 0) throw new IllegalArgumentException("验证码长度必须大于0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
