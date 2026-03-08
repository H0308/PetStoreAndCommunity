package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/09
 * Time: 17:45
 *
 * @Author: 憨八嘎
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPostDetailVO {
    private Long postId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Integer userStatus;
    private Integer banFlag;
    private Long columnId;
    private String columnName;
    private Map<Long, String> topicNames;
    private String title;
    private String content;
    private Long favorCount;
    private Long rejectCount;
    private Long likeCount;
    private Integer status;
    private List<String> mediaUrls;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
