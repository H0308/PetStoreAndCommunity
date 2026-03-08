package org.epsda.pets.service.impl;

import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.service.AdminAuthService;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 21:37
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || userLoginDTO.getPassword() == null ||
                userLoginDTO.getEmail() == null) {
            throw new PetException("用户不存在");
        }

        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();
        // 使用Spring Security进行认证
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginDTO.getEmail(),
                            userLoginDTO.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new PetException("用户名或者密码错误");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new PetException("当前用户不存在");
        }

        if (!user.getRoleId().equals(Constants.ADMIN_FLAG)) {
            throw new PetException("当前用户并非管理员");
        }

        // 生成JWT Token
        String username = user.getUsername();
        String avatarUrl = user.getAvatarUrl();
        String token = JwtUtil.generateToken(user.getId());

        return UserLoginVO.builder()
                .userId(user.getId()).username(username)
                .roleId(user.getRoleId()).avatarUrl(avatarUrl)
                .token(token)
                .build();
    }
}
