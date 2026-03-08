package org.epsda.pets.service.impl;

import com.alibaba.fastjson2.JSON;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.dto.UserRegisterDTO;
import org.epsda.pets.pojo.message.RegisterEmailInfo;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.service.AuthService;
import org.epsda.pets.service.strategy.LoginStrategyContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/13
 * Time: 17:13
 *
 * @Author: 憨八嘎
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private LoginStrategyContext loginStrategyContext;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserLoginVO emailLogin(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || userLoginDTO.getPassword() == null ||
                userLoginDTO.getEmail() == null) {
            throw new PetException("用户不存在");
        }
        // 使用策略模式 - 邮箱密码登录
        return loginStrategyContext.executeLogin("emailLogin", JSON.toJSONString(userLoginDTO));
    }

    @Override
    public UserLoginVO googleLogin(String code) {
        if (!StringUtils.hasText(code)) {
            throw new PetException("Google登录信息错误");
        }
        // 使用策略模式 - Google OAuth 登录
        return loginStrategyContext.executeLogin("googleLogin", code);
    }

    @Override
    public Boolean register(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new PetException("注册信息错误，无法注册用户");
        }

        String username = userRegisterDTO.getUsername();
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();

        User existed = userMapper.selectByUsername(username);
        if (existed != null) {
            throw new PetException("当前用户名已存在使用者");
        }
        existed = userMapper.selectByEmail(email);
        if (existed != null) {
            throw new PetException("当前邮箱已经被使用");
        }

        if (!password.equals(confirmPassword)) {
            throw new PetException("第一次密码和第二次密码不一致");
        }

        // 将密码加密存储到数据库
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        boolean insertRet = userMapper.insert(user) == 1;
        if (!insertRet) {
            throw new PetException("新用户创建失败");
        }

        // 可以考虑发送邮件通知
        RegisterEmailInfo registerEmailInfo =
                new RegisterEmailInfo(email, username);
        rabbitTemplate.convertAndSend("", Constants.REGISTER_EMAIL_QUEUE, registerEmailInfo);

        return true;
    }
}
