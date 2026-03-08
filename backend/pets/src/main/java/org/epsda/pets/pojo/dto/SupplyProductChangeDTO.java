package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:46
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyProductChangeDTO {
    private String brand;
    private String fitAge;
    private String fitVariety;
    private LocalDateTime manufactureDate;
    private LocalDateTime guaranteeDate;
    private String company;
}
