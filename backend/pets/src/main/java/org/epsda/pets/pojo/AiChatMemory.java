package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/03/03
 * Time: 20:47
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_ai_chat_memory")
public class AiChatMemory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private Integer type;
    private String content;
    private LocalDateTime timestamp;
}
