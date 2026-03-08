package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.ProfilePasswordDTO;
import org.epsda.pets.pojo.dto.ProfileReceiptAddressDTO;
import org.epsda.pets.pojo.dto.RealNameAuthDTO;
import org.epsda.pets.pojo.dto.UserProfileChangeDTO;
import org.epsda.pets.pojo.vo.ProfileReceiptAddressVO;
import org.epsda.pets.pojo.vo.UserProfileChangeVO;
import org.epsda.pets.pojo.vo.UserProfileInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 11:10
 *
 * @Author: 憨八嘎
 */
public interface ProfileService {
    UserProfileInfoVO getInfo(Long userId);

    UserProfileChangeVO changeInfo(UserProfileChangeDTO userProfileChangeDTO);

    String uploadAvatar(Long userId, MultipartFile file);

    Boolean realNameAuth(RealNameAuthDTO realNameAuthDTO);

    ProfileReceiptAddressVO getReceiptAddress(Long userId);

    ProfileReceiptAddressVO changeReceiptAddress(ProfileReceiptAddressDTO profileReceiptAddressDTO);

    Boolean changePassword(ProfilePasswordDTO profilePasswordDTO);

    String getPhone(Long userId);

    String changePhone(Long userId, String phone);

    String getEmail(Long userId);

    String changeEmail(Long userId, String email);

    Boolean deactivateAccount(Long userId);
}
