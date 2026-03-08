package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.Topic;
import org.epsda.pets.pojo.vo.PostTopicInfosVO;

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
public interface TopicMapper extends BaseMapper<Topic> {
    @Select("select * from tb_topic where delete_flag = 0")
    List<Topic> selectAllTopicInfo();
}