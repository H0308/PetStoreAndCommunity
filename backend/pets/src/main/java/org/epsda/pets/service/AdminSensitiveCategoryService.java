package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.SensitiveCategoryAddDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryChangeDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListDTO;
import org.epsda.pets.pojo.dto.SensitiveCategoryListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryListVO;
import org.epsda.pets.pojo.vo.SensitiveCategoryVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 19:14
 *
 * @Author: 憨八嘎
 */
public interface AdminSensitiveCategoryService {
    PageVO<SensitiveCategoryListVO> list(SensitiveCategoryListDTO sensitiveCategoryListDTO,
                                         SensitiveCategoryListFilterDTO sensitiveCategoryListFilterDTO);

    List<SensitiveCategoryVO> getAll(Long userId);

    Boolean add(SensitiveCategoryAddDTO sensitiveCategoryAddDTO);

    Boolean change(SensitiveCategoryChangeDTO sensitiveCategoryChangeDTO);

    Boolean delete(Long categoryId, Long userId);

    Boolean batchDelete(List<Long> categoryIds, Long userId);
}
