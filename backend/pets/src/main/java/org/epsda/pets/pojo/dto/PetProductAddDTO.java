package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/30
 * Time: 19:59
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PetProductAddDTO extends BaseProductAddDTO {
    private Integer healthStatus; // 1-健康，2-良好，3-治疗中
    @Length(max = 255, message = "驯养须知最长不超过255个字符")
    private String trainNote;
    @Length(max = 255, message = "领养须知最长不超过255个字符")
    private String raiseNote;
    private Integer vaccineFlag; // 0-未接种，1-已接种
}
