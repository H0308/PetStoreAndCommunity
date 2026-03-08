package org.epsda.pets.service.impl;

import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.ProductSuperMapper;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.SupplyProductBO;
import org.epsda.pets.pojo.vo.BaseProductDetailVO;
import org.epsda.pets.pojo.vo.SupplyDetailProductVO;
import org.epsda.pets.service.ProductService;
import org.epsda.pets.service.SupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 10:28
 *
 * @Author: 憨八嘎
 */
@Service
public class SupplyServiceImpl implements SupplyService {

    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductService productService;

    @Override
    public SupplyDetailProductVO getSupplyDetail(Long productId) {
        // 根据商品ID拿到指定的商品
        // 如果商品ID对应的商品不是宠物用品则抛出异常
        SupplyProductBO supplyProductBO = productSuperMapper.selectSupplyByProductId(productId);
        if (supplyProductBO == null ||
            supplyProductBO.getProductSuper() == null ||
            supplyProductBO.getPetSupplySub() == null) {
            throw new PetException("当前商品ID对应的商品不存在或者对应的商品不是宠物用品");
        }
        PetSupplySub petSupplySub = supplyProductBO.getPetSupplySub(); // 宠物用品子表对象
        BaseProductDetailVO baseDetail =
                productService.getBaseDetail(supplyProductBO.getProductSuper());

        return this.getSupplyDetailProductVO(baseDetail, petSupplySub);
    }

    private SupplyDetailProductVO getSupplyDetailProductVO(BaseProductDetailVO baseDetail, PetSupplySub petSupplySub) {
        SupplyDetailProductVO supplyDetailProductVO = new SupplyDetailProductVO(baseDetail);
        supplyDetailProductVO.setBrand(petSupplySub.getBrand());
        supplyDetailProductVO.setFitAge(petSupplySub.getFitAge());
        supplyDetailProductVO.setFitVariety(petSupplySub.getFitVariety());
        supplyDetailProductVO.setManufactureDate(petSupplySub.getManufactureDate());
        supplyDetailProductVO.setGuaranteeDate(petSupplySub.getGuaranteeDate());
        supplyDetailProductVO.setCompany(petSupplySub.getCompany());
        return supplyDetailProductVO;
    }
}
