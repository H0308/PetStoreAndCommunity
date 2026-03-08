package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.AiChatMemory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/03/03
 * Time: 20:54
 *
 * @Author: 憨八嘎
 */
@Mapper
public interface AiChatMemoryMapper extends BaseMapper<AiChatMemory> {
    @Select("select * from tb_ai_chat_memory where conversation_id = #{conversationId}")
    List<AiChatMemory> selectAllByConversationId(String conversationId);
    @Delete("delete from tb_ai_chat_memory where conversation_id = #{conversationId}")
    void deleteByConversationId(String conversationId);

}
