package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.ProductCommentSub;

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
public interface ProductCommentSubMapper extends BaseMapper<ProductCommentSub> {
    // 根据评论ID删除
    @Delete("delete from tb_product_comment_sub where comment_id = #{commentId}")
    Boolean deleteByCommentId(Long commentId);
}