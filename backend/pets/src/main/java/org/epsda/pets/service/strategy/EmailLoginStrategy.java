package org.epsda.pets.service.strategy;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;
import org.epsda.pets.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("emailLogin")
public class EmailLoginStrategy implements LoginStrategy {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLoginVO login(String credential) {
        UserLoginDTO userLoginDTO = JSON.parseObject(credential, UserLoginDTO.class);
        if (userLoginDTO == null || userLoginDTO.getEmail() == null || userLoginDTO.getPassword() == null) {
            throw new PetException("邮箱或密码不能为空");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userLoginDTO.getEmail(),
                            userLoginDTO.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new PetException("用户名或者密码错误");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, userLoginDTO.getEmail())
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("当前用户ID对应的用户不存在或者已经销户");
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
}
