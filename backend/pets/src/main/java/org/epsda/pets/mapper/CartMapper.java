package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.Cart;

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
public interface CartMapper extends BaseMapper<Cart> {
    // 根据商品ID获取到指定的购物车商品
    @Select("select * from tb_cart where product_id = #{productId} and user_id = #{userId} and delete_flag = 0")
    Cart selectByProductId(Long productId, Long userId);
}