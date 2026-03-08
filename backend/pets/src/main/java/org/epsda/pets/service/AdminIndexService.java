package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.bo.NewUserCountBO;
import org.epsda.pets.pojo.vo.HotTopicVO;
import org.epsda.pets.pojo.vo.OutOfStockProductBO;
import org.epsda.pets.pojo.vo.TopTotalPriceProductVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/27
 * Time: 20:09
 *
 * @Author: 憨八嘎
 */
public interface AdminIndexService {
    Long userCount(Long userId);

    Long productCount(Long userId);

    Long orderCount(Long userId);

    BigDecimal orderPrice(Long userId);

    List<TopTotalPriceProductVO> topTotalPrice(Long userId);

    List<NewUserCountBO> newUserCount(Long userId);

    List<HotTopicVO> hotTopic(@NotNull Long userId);

    List<OutOfStockProductBO> outOfStock(Long userId);
}
