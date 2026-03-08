package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class PetProductChangeDTO {
    private Integer healthStatus; // 1-健康，2-良好，3-治疗中
    private String trainNote;
    private String raiseNote;
    private Integer vaccineFlag; // 0-未接种，1-已接种
}
