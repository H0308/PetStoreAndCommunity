package org.epsda.pets.service;

import org.epsda.pets.pojo.vo.SupplyDetailProductVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 10:28
 *
 * @Author: 憨八嘎
 */
public interface SupplyService {
    SupplyDetailProductVO getSupplyDetail(Long productId);
}
