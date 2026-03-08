package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.mapper.RoleMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Role;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.security.CustomUserDetails;
import org.epsda.pets.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/20
 * Time: 22:20
 *
 * @Author: 憨八嘎
 */
@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 从数据库查询用户信息
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + email);
        }

        // 查询用户角色信息
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new UsernameNotFoundException("用户角色不存在");
        }

        // 构建用户权限列表
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        // 返回Spring Security的User对象
        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getId(), user.getRoleId(), authorities);
    }

    @Override
    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: userId=" + userId);
        }

        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new UsernameNotFoundException("用户角色不存在");
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getId(), user.getRoleId(), authorities);
    }
}
