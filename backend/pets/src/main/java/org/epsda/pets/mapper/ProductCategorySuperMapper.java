package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.ProductCategorySuper;

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
public interface ProductCategorySuperMapper extends BaseMapper<ProductCategorySuper> {
    // 查找所有宠物分类（按照类型进行排序，默认宠物在上，用品在下）
    @Select("select * from `tb_product_category_super` where delete_flag = 0 order by type asc")
    List<ProductCategorySuper> selectAllPetCategories();
    // 根据ID获取到一级分类
    @Select("select * from `tb_product_category_super` where id = #{categoryId} and delete_flag = 0")
    ProductCategorySuper selectCategoryByCategoryId(Long categoryId);
}