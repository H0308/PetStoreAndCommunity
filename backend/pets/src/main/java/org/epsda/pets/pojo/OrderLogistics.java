package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/05
 * Time: 16:44
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_order_logistics")
public class OrderLogistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long orderId;
    private Integer transportType;
    @Length(max = 255, message = "纬度最长不超过255个字符")
    private String originLat;
    @Length(max = 255, message = "经度最长不超过255个字符")
    private String originLng;
    @Length(max = 255, message = "纬度最长不超过255个字符")
    private String destLat;
    @Length(max = 255, message = "经度最长不超过255个字符")
    private String destLng;
    @Length(max = 255, message = "纬度最长不超过255个字符")
    private String currLat;
    @Length(max = 255, message = "经度最长不超过255个字符")
    private String currLng;
    private Integer deleteFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
