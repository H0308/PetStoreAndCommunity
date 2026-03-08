package org.epsda.pets.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 16:20
 *
 * @Author: 憨八嘎
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailListWithFilterDTO {
    private UserDetailListDTO userDetailListDTO;
    private UserDetailFilterDTO userDetailFilterDTO;
}
