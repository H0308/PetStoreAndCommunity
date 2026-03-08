package org.epsda.pets.pojo.vo;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/12
 * Time: 20:06
 *
 * @Author: 憨八嘎
 */
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCommentVO extends BaseCommentVO {
    private Long stars;

    public ProductCommentVO(BaseCommentVO baseCommentVO) {
        super(baseCommentVO.getCommentId(), baseCommentVO.getUserId(),
              baseCommentVO.getUsername(), baseCommentVO.getAvatarUrl(),
              baseCommentVO.getParentId(), baseCommentVO.getContent(),
              baseCommentVO.getStatus(), baseCommentVO.getDeleteFlag(),
              baseCommentVO.getMediaVOS(), baseCommentVO.getUpdateTime());
    }
}
