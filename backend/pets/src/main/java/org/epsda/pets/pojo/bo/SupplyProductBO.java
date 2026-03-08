package org.epsda.pets.pojo.bo;

import lombok.*;
import org.epsda.pets.pojo.PetSupplySub;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/11
 * Time: 11:57
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplyProductBO extends BaseProductBO {
    private PetSupplySub petSupplySub;
}
