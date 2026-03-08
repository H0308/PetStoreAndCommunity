package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/04
 * Time: 15:11
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListWithFilterDTO {
    private OrderListDTO orderListDTO;
    private OrderListFilterDTO orderListFilterDTO;
}
