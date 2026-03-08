package org.epsda.pets.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 15:57
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MessageListDTO extends PageDTO {
    @NotNull
    private Long userId;
    @NotNull
    private Long serverId;
}
