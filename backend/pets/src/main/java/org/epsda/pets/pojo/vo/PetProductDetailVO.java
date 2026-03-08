package org.epsda.pets.pojo.vo;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 11:16
 *
 * @Author: 憨八嘎
 */
@SuperBuilder // 确保子类可以设置父类
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PetProductDetailVO extends BaseProductDetailVO {
    private Integer healthStatus;
    private String trainNote;
    private String raiseNote;
    private Integer vaccineFlag;

    public PetProductDetailVO(BaseProductDetailVO baseProductDetailVO) {
        super(baseProductDetailVO.getProductId(),
                baseProductDetailVO.getIdentifier(),
                baseProductDetailVO.getProductType(),
                baseProductDetailVO.getDeliverAddress(),
                baseProductDetailVO.getName(),
                baseProductDetailVO.getDescription(),
                baseProductDetailVO.getMainCategoryName(),
                baseProductDetailVO.getSubCategoryName(),
                baseProductDetailVO.getStatus(),
                baseProductDetailVO.getPrice(),
                baseProductDetailVO.getStock(),
                baseProductDetailVO.getProductImages());
    }
}
