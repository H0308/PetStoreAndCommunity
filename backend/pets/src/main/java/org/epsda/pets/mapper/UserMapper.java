package org.epsda.pets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.bo.NewUserCountBO;

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
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from tb_user where phone = #{phone} and delete_flag = 0 and status = 0")
    User selectByPhone(String phone);
    @Select("select * from tb_user where email = #{email} and delete_flag = 0 and status = 0")
    User selectByEmail(String email);
    @Select("select * from tb_user where id_card = #{idCard} and delete_flag = 0 and status = 0")
    User selectByIdCard(String idCard);
    @Select("select * from tb_user where username = #{username} and delete_flag = 0 and status = 0")
    User selectByUsername(String username);
    @Select("select date(create_time) as createTime, count(*) as count from tb_user where create_time >= date_sub(curdate(), interval 31 day) group by date(create_time)")
    List<NewUserCountBO> selectNewUserCountByCreateTime();
}