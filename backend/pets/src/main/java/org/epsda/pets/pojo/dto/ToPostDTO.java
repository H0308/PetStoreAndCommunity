package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.epsda.pets.constants.Constants;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/20
 * Time: 10:52
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToPostDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long columnId; // 不可为null
    private List<Long> topicIds; // 可以为null
    @NotNull
    private String title;
    @NotNull
    private String content;
    @NotNull
    // 默认0为保存草稿操作（前端需要询问用户是否保存草稿），如果用户点击发布需要传递1表示发布
    private Integer opFlag;
}
