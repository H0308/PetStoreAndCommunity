package org.epsda.pets.pojo.bo;

import lombok.*;
import org.epsda.pets.pojo.PetSub;

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
public class PetProductBO extends BaseProductBO {
    private PetSub petSub;
}
