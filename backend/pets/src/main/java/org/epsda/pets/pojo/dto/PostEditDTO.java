package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/20
 * Time: 21:15
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEditDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long postId;
    private Long columnId;
    private List<Long> topicIds; // 可以为空
    private String title;
    private String content;
    @NotNull
    private Map<Long, Integer> mediaUrlWithDeleteFlag; // 0为不删除，1为删除
    @NotNull
    private Integer opFlag; // 默认0为草稿，1为发布
}
