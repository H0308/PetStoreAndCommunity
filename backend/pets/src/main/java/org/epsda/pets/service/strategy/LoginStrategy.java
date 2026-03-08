package org.epsda.pets.service.strategy;

import org.epsda.pets.pojo.vo.UserLoginVO;

/**
 * 登录策略接口 - 策略模式
 * 不同的登录方式实现此接口
 */
public interface LoginStrategy {
    // 邮箱密码登录传登录信息Json字符串，OAuth2登录传回调认证code
    UserLoginVO login(String credential);
}
