package org.epsda.pets.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 19:32
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchListDTO extends PageDTO {
    @NotNull
    private String keyword; // 商品-商品名称，帖子-帖子标题
}
