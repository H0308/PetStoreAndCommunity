package org.epsda.pets.service.strategy;

import org.epsda.pets.exception.PetException;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 登录策略上下文 - 根据登录类型选择对应策略
 */
@Component
public class LoginStrategyContext {

    // Spring特性：注入Map<String, SomeInterface>时，String是Bean名称，value是Bean实例
    // 当前strategyMap为
    // { GoogleLoginStrategy（存储的Bean名称：googleLogin）, GoogleLoginStrategy }
    // { EmailLoginStrategy（存储的Bean名称：emailLogin）, EmailLoginStrategy }
    @Autowired
    private Map<String, LoginStrategy> strategyMap;

    // 根据指定Bean名称调用对应的登录策略
    public UserLoginVO executeLogin(String loginType, String credential) {
        LoginStrategy strategy = strategyMap.get(loginType);
        if (strategy == null) {
            throw new PetException("不支持的登录方式: " + loginType);
        }
        return strategy.login(credential);
    }
}
