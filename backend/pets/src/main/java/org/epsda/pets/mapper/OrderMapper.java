package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.Order;
import org.epsda.pets.pojo.bo.OrderCountBO;
import org.epsda.pets.pojo.bo.OrderIdWithTotalPriceBO;

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
public interface OrderMapper extends BaseMapper<Order> {
    @Select("select product_id, count(*) sell_count from tb_order group by product_id order by sell_count asc")
    List<OrderCountBO> selectAllOrdersByOrderCountAsc();
    @Select("select product_id, count(*) sell_count from tb_order group by product_id order by sell_count desc")
    List<OrderCountBO> selectAllOrdersByOrderCountDesc();
    @Select("select product_id, sum(total_price) as product_total from tb_order where status = 1 group by product_id;")
    List<OrderIdWithTotalPriceBO> selectTop10TotalPrice();
}