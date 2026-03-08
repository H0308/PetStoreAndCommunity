package org.epsda.pets.service.impl;

import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.*;
import org.epsda.pets.pojo.*;
import org.epsda.pets.pojo.bo.PetProductBO;
import org.epsda.pets.pojo.vo.BaseProductDetailVO;
import org.epsda.pets.pojo.vo.PetProductDetailVO;
import org.epsda.pets.service.PetService;
import org.epsda.pets.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 11:38
 *
 * @Author: 憨八嘎
 */
@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private ProductSuperMapper productSuperMapper;
    @Autowired
    private ProductService productService;

    @Override
    public PetProductDetailVO getPetDetail(Long productId) {
        // 根据商品ID拿到商品数据
        // 先判断商品ID是否为宠物商品，如果不是则直接抛出异常
        PetProductBO petProductBO = productSuperMapper.selectPetByProductId(productId);
        if (petProductBO == null ||
            petProductBO.getProductSuper() == null ||
            petProductBO.getPetSub() == null) {
            throw new PetException("当前商品ID对应的商品不存在或者对应的商品不是宠物");
        }
        PetSub petSub = petProductBO.getPetSub(); // 获取商品子表对象
        BaseProductDetailVO baseDetail =
                productService.getBaseDetail(petProductBO.getProductSuper()); // 获取到公共的详细信息

        return this.getPetProductDetailVO(baseDetail, petSub);
    }

    private PetProductDetailVO getPetProductDetailVO(BaseProductDetailVO baseDetail, PetSub petSub) {
        PetProductDetailVO petProductDetailVO = new PetProductDetailVO(baseDetail);
        petProductDetailVO.setHealthStatus(petSub.getHealthStatus());
        petProductDetailVO.setTrainNote(petSub.getTrainNote());
        petProductDetailVO.setRaiseNote(petSub.getRaiseNote());
        petProductDetailVO.setVaccineFlag(petSub.getVaccineFlag());
        return petProductDetailVO;
    }
}
