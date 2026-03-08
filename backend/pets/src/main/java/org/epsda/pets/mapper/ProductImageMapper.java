package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.ProductImage;

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
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    // 获取所有主图
    @Select("select * from tb_product_image where main_flag = 1 and delete_flag = 0")
    List<ProductImage> selectAllMainImage();

    // 根据商品ID获取到商品图片，非主图
    @Select("select * from tb_product_image where main_flag = 0 and delete_flag = 0 and product_id = #{productId}")
    List<ProductImage> selectImagesByProductId(Long productId);
}