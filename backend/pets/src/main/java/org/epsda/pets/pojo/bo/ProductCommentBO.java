package org.epsda.pets.pojo.bo;

import lombok.*;
import org.epsda.pets.pojo.ProductCommentSub;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 20:33
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentBO extends BaseCommentBO{
    private ProductCommentSub productCommentSub;
}
