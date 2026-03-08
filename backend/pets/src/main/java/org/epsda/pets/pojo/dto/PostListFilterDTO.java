package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/08
 * Time: 19:52
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostListFilterDTO {
    private Long columnId;
    private Long topicId;
    private String title;
    private Long userId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
