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
 * Date: 2026/01/04
 * Time: 17:03
 *
 * @Author: 憨八嘎
 */
@Data
@TableName("tb_refund_info")
public class RefundInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long orderId;
    @NotNull
    private Long userId;
    @Length(max = 2048, message = "退款原因最大长度不超过2048个字符")
    private String message;
    private Integer deleteFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
