package org.epsda.pets.service;

import org.epsda.pets.pojo.vo.PetProductDetailVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 11:38
 *
 * @Author: 憨八嘎
 */
public interface PetService {
    PetProductDetailVO getPetDetail(Long productId);
}
