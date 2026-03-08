package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 10:29
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplyDetailProductVO extends BaseProductDetailVO {
    private String brand;
    private String fitAge;
    private String fitVariety;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime manufactureDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime guaranteeDate;
    private String company;

    public SupplyDetailProductVO(BaseProductDetailVO baseProductDetailVO) {
        super(baseProductDetailVO.getProductId(), baseProductDetailVO.getIdentifier(),
              baseProductDetailVO.getProductType(), baseProductDetailVO.getDeliverAddress(),
              baseProductDetailVO.getName(), baseProductDetailVO.getDescription(),
              baseProductDetailVO.getMainCategoryName(), baseProductDetailVO.getSubCategoryName(),
              baseProductDetailVO.getStatus(), baseProductDetailVO.getPrice(),
              baseProductDetailVO.getStock(), baseProductDetailVO.getProductImages());
    }
}
