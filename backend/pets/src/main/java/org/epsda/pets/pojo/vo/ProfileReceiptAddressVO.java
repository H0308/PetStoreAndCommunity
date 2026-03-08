package org.epsda.pets.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/23
 * Time: 10:39
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileReceiptAddressVO {
    private Long receiptId;
    private String receiptName;
    private String receiptAddress;
}
