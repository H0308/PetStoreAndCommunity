package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.UserDetailChangeDTO;
import org.epsda.pets.pojo.dto.UserDetailListDTO;
import org.epsda.pets.pojo.dto.UserDetailFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.UserDetailListVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 15:12
 *
 * @Author: 憨八嘎
 */
public interface AdminUserService {
    PageVO<UserDetailListVO> list(UserDetailListDTO userDetailListDTO, UserDetailFilterDTO userDetailFilterDTO);

    Boolean change(UserDetailChangeDTO userDetailChangeDTO);

    Boolean reset(Long userIdChange, Long userId);

    Boolean deactivate(Long userIdChange, Long userId);

    Boolean delete(Long userIdChange, Long userId);

    Boolean batchDelete(List<Long> userIdChanges, Long userId);

    Boolean ban(Long userIdChange, Long userId);

    Boolean batchBan(List<Long> userIdChanges, Long userId);

    Boolean unBan(@NotNull Long userIdChange, @NotNull Long userId);

    Boolean batchUnBan(List<Long> userIdChanges, Long userId);
}
