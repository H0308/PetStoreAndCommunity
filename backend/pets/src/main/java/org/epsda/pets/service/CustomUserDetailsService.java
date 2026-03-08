package org.epsda.pets.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/20
 * Time: 22:20
 *
 * @Author: 憨八嘎
 */
public interface CustomUserDetailsService extends UserDetailsService {
    UserDetails loadUserById(Long userId);
}
