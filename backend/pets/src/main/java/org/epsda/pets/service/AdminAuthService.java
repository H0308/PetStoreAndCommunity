package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.UserLoginDTO;
import org.epsda.pets.pojo.vo.UserLoginVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 21:37
 *
 * @Author: 憨八嘎
 */
public interface AdminAuthService {
    UserLoginVO login(UserLoginDTO userLoginDTO);
}
