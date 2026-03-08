package org.epsda.pets.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 16:51
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicChangeDTO {
    @NotNull
    private Long topicId; // 不可变
    @NotNull
    private Long userId; // 不可变
    @Length(max = 25, message = "话题长度最长不超过25个字符")
    private String topicName;
}
