package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.ColumnAddDTO;
import org.epsda.pets.pojo.dto.ColumnChangeDTO;
import org.epsda.pets.pojo.dto.ColumnListDTO;
import org.epsda.pets.pojo.dto.ColumnListFilterDTO;
import org.epsda.pets.pojo.vo.ColumnListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.TopicListVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 17:31
 *
 * @Author: 憨八嘎
 */
public interface AdminColumnService {
    PageVO<ColumnListVO> list(ColumnListDTO columnListDTO, ColumnListFilterDTO columnListFilterDTO);

    Boolean change(ColumnChangeDTO columnChangeDTO);

    Boolean add(ColumnAddDTO columnAddDTO);

    Boolean delete(Long columnId, Long userId);

    Boolean batchDelete(List<Long> columnIds, Long userId);
}
