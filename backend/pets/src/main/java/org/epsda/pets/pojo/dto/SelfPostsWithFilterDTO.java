package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.epsda.pets.pojo.vo.SelfPostVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/20
 * Time: 17:20
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfPostsWithFilterDTO {
    private SelfPostDTO selfPostDTO;
    private SelfPostFilterDTO selfPostFilterDTO;
}
