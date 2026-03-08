package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/13
 * Time: 16:18
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentListFilterDTO {
    private Long commentId;
    private String content;
    private String username;
    private Long objectType;
    private String objectName;
    private Integer status;
    private Integer deleteFlag;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
