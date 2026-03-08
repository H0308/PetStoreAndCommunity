package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:21
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_address")
public class Address {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    @UniqueElements
    @Length(max = 255, message = "地址最长不超过255个字符")
    private String addressText;
    @NotNull
    @Length(max = 255, message = "纬度最长不超过255个字符")
    private String latitude;
    @NotNull
    @Length(max = 255, message = "经度最长不超过255个字符")
    private String longitude;
    private Integer deleteFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}