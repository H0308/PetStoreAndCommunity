package org.epsda.pets.pojo.vo;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:07
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminPetDetailVO extends AdminBaseProductDetailVO{
    private Integer healthStatus; // 1-健康，2-良好，3-治疗中
    private String trainNote;
    private String raiseNote;
    private Integer vaccineFlag; // 0-未接种，1-已接种

    public AdminPetDetailVO(AdminBaseProductDetailVO adminBaseProductDetailVO) {
        super(adminBaseProductDetailVO.getIdentifier(),
                adminBaseProductDetailVO.getProductName(),
                adminBaseProductDetailVO.getDescription(),
                adminBaseProductDetailVO.getType(),
                adminBaseProductDetailVO.getShipAddress(),
                adminBaseProductDetailVO.getMainCategoryId(),
                adminBaseProductDetailVO.getSubCategoryId(),
                adminBaseProductDetailVO.getPrice(),
                adminBaseProductDetailVO.getStock(),
                adminBaseProductDetailVO.getStatus(),
                adminBaseProductDetailVO.getProductImageUrls()
        );
    }
}
