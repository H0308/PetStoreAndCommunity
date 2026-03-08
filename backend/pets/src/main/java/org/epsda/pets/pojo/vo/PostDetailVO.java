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
 * Date: 2025/12/18
 * Time: 20:18
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDetailVO {
    private Long postId;
    private Long userId;
    private Long columnId;
    private String columnName;
    private String avatar;
    private String username;
    private List<Long> topicId;
    private List<String> topicName;
    private String title;
    private String content;
    private Long favorCount;
    private Long likeCount;
    private Long rejectCount;
    private Map<Long, String> mediaUrlWithId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
