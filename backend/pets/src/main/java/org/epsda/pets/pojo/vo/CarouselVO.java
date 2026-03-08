package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/09
 * Time: 16:44
 *
 * @Author: 憨八嘎
 */
@Builder // 使用建造者模式
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarouselVO {
    private Long productId;
    private String imageUrl;
    private Integer productType;
}
