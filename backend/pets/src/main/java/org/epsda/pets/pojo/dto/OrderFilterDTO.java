package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/15
 * Time: 18:41
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFilterDTO {
    private Integer latest;
    private Integer status;
    private Integer refundFlag;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
