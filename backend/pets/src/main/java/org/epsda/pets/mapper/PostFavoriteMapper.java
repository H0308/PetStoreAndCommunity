package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.PostFavorite;

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
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {
    @Select("select * from tb_post_favorite where user_id = #{userId} and post_id = #{postId}")
    PostFavorite selectByUserIdAndPostId(Long userId, Long postId);
}