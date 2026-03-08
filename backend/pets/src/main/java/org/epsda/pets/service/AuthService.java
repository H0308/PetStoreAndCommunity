package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.dto.UserRegisterDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/13
 * Time: 17:12
 *
 * @Author: 憨八嘎
 */
public interface AuthService {
    UserLoginVO emailLogin(UserLoginDTO userLoginDTO);

    UserLoginVO googleLogin(String code);

    Boolean register(UserRegisterDTO userRegisterDTO);
}
