package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.TopicPost;
import org.epsda.pets.pojo.bo.HotTopicBO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 11:52
 *
 * @Author: 憨八嘎
 */
@Mapper
public interface TopicPostMapper extends BaseMapper<TopicPost> {
    @Select("select * from tb_topic_post where post_id = #{postId} and delete_flag = 0")
    List<TopicPost> selectTopicsByPostId(Long postId);
    @Select("select * from tb_topic_post where topic_id = #{topicId} and delete_flag = 0")
    List<TopicPost> selectTopicsByTopicId(Long topicId);
    @Select("select topic_id, count(*) as count from tb_topic_post where delete_flag = 0 group by topic_id order by count desc limit 5;")
    List<HotTopicBO> selectTop5HotTopic();
}