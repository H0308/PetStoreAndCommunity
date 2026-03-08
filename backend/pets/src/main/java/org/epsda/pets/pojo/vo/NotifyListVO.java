package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 20:22
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyListVO {
    private Long notifyId;
    private String title;
    private String content;
    private Integer type;
    private Integer status;
    private LocalDateTime createTime;
}
