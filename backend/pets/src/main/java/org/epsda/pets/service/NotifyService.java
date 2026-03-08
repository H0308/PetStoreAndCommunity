package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.NotifyListDTO;
import org.epsda.pets.pojo.vo.NotifyListVO;
import org.epsda.pets.pojo.vo.PageVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 20:18
 *
 * @Author: 憨八嘎
 */
public interface NotifyService {
    PageVO<NotifyListVO> list(NotifyListDTO notifyListDTO);

    Boolean read(Long userId, Long notifyId);

    Boolean readAll(Long userId, List<Long> notifyIds);

    Long unreadCount(Long userId);

    Boolean delete(Long userId, Long notifyId);
}
