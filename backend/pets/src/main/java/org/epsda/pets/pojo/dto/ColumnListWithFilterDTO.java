package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 17:35
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnListWithFilterDTO {
    private ColumnListDTO columnListDTO;
    private ColumnListFilterDTO columnListFilterDTO;
}
