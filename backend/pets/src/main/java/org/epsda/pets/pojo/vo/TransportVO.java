package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/05
 * Time: 17:27
 *
 * @Author: 憨八嘎
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TransportVO {
    private Long logisticsId;
    private Integer transportType; // 运输类型：1-空运 2-陆运
    private Integer orderStatus;   // 订单状态：3-已发货 4-待收货
    private String originLat;
    private String originLng;
    private String destLat;
    private String destLng;
    private String currentLat;
    private String currentLng;
}
