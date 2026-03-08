package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:23
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    @Length(max = 30, message = "用户名长度最长不超过30个字符")
    private String username;
    @NotNull
    @Length(max = 30, message = "邮箱长度最长不超过30个字符")
    private String email;
    @NotNull
    @Length(max = 30, message = "密码长度最长不超过30个字符")
    private String password;
    private Integer gender;
    @Length(max = 30, message = "用户真实姓名长度最长不超过30个字符")
    private String realName;
    @Length(max = 30, message = "身份证号码长度最长不超过30个字符")
    private String idCard;
    @NotNull
    @Length(max = 30, message = "手机号长度最长不超过30个字符")
    private String phone;
    @NotNull
    @Length(max = 255, message = "图片链接长度最长不超过255个字符")
    private String avatarUrl;
    private Long roleId;
    @Length(max = 30, message = "收货人名称长度最长不超过30个字符")
    private String receiptName;
    private Long receiptId;
    @Length(max = 255, message = "个人简介长度最长不超过255个字符")
    private String profile;
    private Integer status;
    private Integer banFlag;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}