package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.ProductSuper;
import org.epsda.pets.pojo.bo.PetProductBO;
import org.epsda.pets.pojo.bo.SupplyProductBO;

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
public interface ProductSuperMapper extends BaseMapper<ProductSuper> {
    // 根据创建时间降序获取到宠物
    @Select("select * from tb_product_super where delete_flag = 0 and status != 3 order by create_time desc limit 9")
    List<ProductSuper> selectAllPetsByCreateTimeDesc();
    // 根据主分类ID获取到该分类下所有的商品
    @Select("select * from tb_product_super where main_category_id = #{mainCategoryId} and delete_flag = 0 and status != 3")
    List<ProductSuper> selectProductsByMainCategoryId(Long mainCategoryId);
    // 根据商品二级分类ID获取到该分类下所有的商品
    @Select("select * from tb_product_super where sub_category_id = #{subCategoryId} and delete_flag = 0 and status != 3")
    List<ProductSuper> selectBySubCategoryId(Long subCategoryId);
    // 根据商品名称进行模糊搜索
    @Select("select id from tb_product_super where name like concat('%', #{keyword}, '%') and delete_flag = 0 and status != 3")
    List<Long> selectProductIdsByProductName(String keyword);
    // 根据商品ID获取到指定商品数据
    PetProductBO selectPetByProductId(Long productId);
    SupplyProductBO selectSupplyByProductId(Long productId);
}