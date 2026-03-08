package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.SensitiveWordAddDTO;
import org.epsda.pets.pojo.dto.SensitiveWordListDTO;
import org.epsda.pets.pojo.dto.SensitiveWordListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SensitiveWordListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:43
 *
 * @Author: 憨八嘎
 */
public interface AdminSensitiveWordService {
    PageVO<SensitiveWordListVO> list(SensitiveWordListDTO sensitiveWordListDTO, SensitiveWordListFilterDTO sensitiveWordListFilterDTO);

    Boolean add(SensitiveWordAddDTO sensitiveWordAddDTO);

    Long addWithCSV(MultipartFile file, Long userId);

    Boolean delete(Long wordId, Long userId);

    Boolean batchDelete(List<Long> wordIds, Long userId);
}
