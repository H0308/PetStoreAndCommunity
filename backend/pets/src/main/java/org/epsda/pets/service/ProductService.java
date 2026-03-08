package org.epsda.pets.service;

import org.epsda.pets.pojo.ProductSuper;
import org.epsda.pets.pojo.vo.BaseProductDetailVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 11:16
 *
 * @Author: 憨八嘎
 */
public interface ProductService {
    BaseProductDetailVO getBaseDetail(ProductSuper productSuper);
}
