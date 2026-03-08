package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.PostMedia;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 11:51
 *
 * @Author: 憨八嘎
 */
@Mapper
public interface PostMediaMapper extends BaseMapper<PostMedia> {
    @Select("select * from tb_post_media where post_id = #{postId} and delete_flag = 0")
    List<PostMedia> selectMediaByPostId(Long postId);
}