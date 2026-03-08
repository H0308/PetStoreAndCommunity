package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.CommentMedia;

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
public interface CommentMediaMapper extends BaseMapper<CommentMedia> {
    // 根据评论ID获取到评论媒体
    @Select("select * from tb_comment_media where comment_id = #{commentId} and delete_flag = 0")
    List<CommentMedia> selectMediaByCommentId(Long commentId);
}