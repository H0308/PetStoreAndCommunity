package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/31
 * Time: 19:50
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MainProductChangeDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    private BaseProductChangeDTO baseProductChangeDTO;
    private PetProductChangeDTO petProductChangeDTO;
    private SupplyProductChangeDTO supplyProductChangeDTO;
}
