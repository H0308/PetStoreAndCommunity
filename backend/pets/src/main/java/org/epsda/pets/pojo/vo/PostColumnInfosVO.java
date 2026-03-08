package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/18
 * Time: 12:10
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostColumnInfosVO {
    private Long columnId;
    private String name;
}
