package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.ProductCategorySub;

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
public interface ProductCategorySubMapper extends BaseMapper<ProductCategorySub> {
    // 根据指定一级分类ID获取所有的二级分类
    @Select("select * from tb_product_category_sub where main_category_id = #{mainCategoryId} and delete_flag = 0")
    List<ProductCategorySub> selectAllSubCategoriesByMainCategoryId(Long mainCategoryId);
    // 根据二级分类ID获取分类
    @Select("select * from tb_product_category_sub where id = #{categoryId} and delete_flag = 0")
    ProductCategorySub selectSubCategoryByCategoryId(Long categoryId);
}