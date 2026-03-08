package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:10
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminSupplyDetailVO extends AdminBaseProductDetailVO{
    private String brand;
    private String fitAge;
    private String fitVariety;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime manufactureDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime guaranteeDate;
    private String company;

    public AdminSupplyDetailVO(AdminBaseProductDetailVO adminBaseProductDetailVO) {
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
