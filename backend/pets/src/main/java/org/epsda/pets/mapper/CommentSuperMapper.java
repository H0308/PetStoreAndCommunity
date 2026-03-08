package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.CommentSuper;
import org.epsda.pets.pojo.bo.ProductCommentBO;

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
public interface CommentSuperMapper extends BaseMapper<CommentSuper> {
    // 根据对象ID获取到指定的商品评论
    List<ProductCommentBO> selectProductCommentsByObjectId(Long objectId);
    // 根据对象ID获取到指定的帖子评论
    @Select("select * from tb_comment_super where object_id = #{objectId} and type = 2 and status = 2")
    List<CommentSuper> selectPostCommentsByObjectId(Long objectId);
}