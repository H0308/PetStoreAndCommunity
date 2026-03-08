package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.PostLike;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/21
 * Time: 12:20
 *
 * @Author: 憨八嘎
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
    @Select("select * from tb_post_like where user_id = #{userId} and post_id = #{postId}")
    PostLike selectByUserIdAndPostId(Long userId, Long postId);
}
