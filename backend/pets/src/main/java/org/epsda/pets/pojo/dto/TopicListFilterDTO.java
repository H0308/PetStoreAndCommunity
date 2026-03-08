package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.epsda.pets.constants.Constants;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 9:59
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicListFilterDTO {
    private String topicName;
    private Integer sortFlag = Constants.DESC_FLAG; // 1-降序，2-升序，默认降序
}
