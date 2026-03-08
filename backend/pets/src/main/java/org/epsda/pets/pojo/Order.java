package org.epsda.pets.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
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
@TableName("tb_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    @NotNull
    @Length(max = 30, message = "额外手机号码长度不超过30个字符")
    private String phone;
    @NotNull
    @Length(max = 30, message = "收货人名称长度不超过30个字符")
    private String receiptName;
    @NotNull
    private Long receiptId;
    private Long totalCount;
    private BigDecimal totalPrice;
    private Integer status;
    private Integer refundFlag;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}