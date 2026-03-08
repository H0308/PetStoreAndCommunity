package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/05
 * Time: 18:16
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransportArrivedVO {
    private Boolean isArrived;
    private Boolean successFlag;
}
