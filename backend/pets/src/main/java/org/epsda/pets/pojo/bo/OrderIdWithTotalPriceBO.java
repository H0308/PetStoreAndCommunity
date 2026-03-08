package org.epsda.pets.pojo.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 22:11
 *
 * @Author: 憨八嘎
 */
@Data
public class OrderIdWithTotalPriceBO {
    private Long productId;
    private BigDecimal productTotal;
}
