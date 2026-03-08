package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/10
 * Time: 17:40
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveWordListWithFilterDTO {
    private SensitiveWordListDTO sensitiveWordListDTO;
    private SensitiveWordListFilterDTO sensitiveWordListFilterDTO;
}
