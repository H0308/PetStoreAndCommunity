package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.epsda.pets.pojo.vo.SensitiveCategoryListVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/11
 * Time: 20:28
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveCategoryListWithFilterDTO {
    private SensitiveCategoryListDTO sensitiveCategoryListDTO;
    private SensitiveCategoryListFilterDTO sensitiveCategoryListFilterDTO;
}
