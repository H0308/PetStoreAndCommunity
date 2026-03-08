package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.MessageMedia;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/26
 * Time: 20:05
 *
 * @Author: 憨八嘎
 */
@Mapper
public interface MessageMediaMapper extends BaseMapper<MessageMedia> {
    // 根据messageId获取到聊天媒体文件
    // 每一条文件记录对应着一条消息
    @Select("select * from tb_message_media where message_id = #{messageId} and delete_flag = 0")
    MessageMedia selectMessageMediaByMessageId(Long messageId);
}
