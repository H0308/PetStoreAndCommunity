package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.AddressMapper;
import org.epsda.pets.mapper.CartMapper;
import org.epsda.pets.mapper.OrderMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Address;
import org.epsda.pets.pojo.Cart;
import org.epsda.pets.pojo.Order;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.ProfilePasswordDTO;
import org.epsda.pets.pojo.dto.ProfileReceiptAddressDTO;
import org.epsda.pets.pojo.dto.RealNameAuthDTO;
import org.epsda.pets.pojo.dto.UserProfileChangeDTO;
import org.epsda.pets.pojo.message.NotifyEmailInfo;
import org.epsda.pets.pojo.vo.ProfileReceiptAddressVO;
import org.epsda.pets.pojo.vo.UserProfileChangeVO;
import org.epsda.pets.pojo.vo.UserProfileInfoVO;
import org.epsda.pets.service.OrderService;
import org.epsda.pets.service.ProfileService;
import org.epsda.pets.utils.BaiduMapUtil;
import org.epsda.pets.utils.OSSUploadFileUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/22
 * Time: 11:10
 *
 * @Author: 憨八嘎
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private OSSUploadFileUtil uploadFileUtil;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private BaiduMapUtil baiduMapUtil;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public UserProfileInfoVO getInfo(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户ID错误，无法获取用户信息");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("指定用户不存在，获取用户信息失败");
        }

        String username = user.getUsername();
        String avatarUrl = user.getAvatarUrl();
        String profile = user.getProfile();
        String realName = user.getRealName();
        Integer gender = user.getGender();
        Long roleId = user.getRoleId();
        Integer banFlag = user.getBanFlag();
        String idCard = user.getIdCard();

        return UserProfileInfoVO.builder()
                .username(username).avatarUrl(avatarUrl)
                .gender(gender).roleId(roleId).banFlag(banFlag)
                .realNameAuthFlag(StringUtils.hasText(idCard) && StringUtils.hasText(realName) ?
                        Constants.REAL_NAME_AUTHENTICATED_FLAG :
                        Constants.REAL_NAME_NOT_AUTHENTICATE_FLAG)
                .profile(profile).build();
    }

    @Override
    public UserProfileChangeVO changeInfo(UserProfileChangeDTO userProfileChangeDTO) {
        if (userProfileChangeDTO == null || userProfileChangeDTO.getUserId() == null) {
            throw new PetException("用户信息错误，无法修改个人信息");
        }

        Long userId = userProfileChangeDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        String username = userProfileChangeDTO.getUsername();
        String profile = userProfileChangeDTO.getProfile();
        Integer gender = userProfileChangeDTO.getGender();

        User oldUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (oldUser == null) {
            throw new PetException("指定用户不存在，修改信息失败");
        }

        User newUser = new User();
        newUser.setId(userId);
        if(StringUtils.hasText(username)) {
            newUser.setUsername(username);
        }

        if (StringUtils.hasText(profile)) {
            newUser.setProfile(profile);
        }

        if (gender != null && gender > 0 && gender < 4) {
            newUser.setGender(gender);
        }

        // 更新
        boolean updateRet = userMapper.updateById(newUser) == 1;
        if (!updateRet) {
            throw new PetException("修改用户个人信息错误");
        }

        return UserProfileChangeVO.builder()
                .username(username).gender(gender)
                .profile(profile).build();
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null ||
                userId == null) {
            throw new PetException("上传的文件不合法");
        }

        // 上传文件
        String fileUrl = uploadFileUtil.uploadFileToOss(file);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new PetException("当前用户ID对应的不存在");
        }

        User uploadAvatarUser = new User();
        uploadAvatarUser.setId(userId);
        uploadAvatarUser.setAvatarUrl(fileUrl);
        boolean updateRet = userMapper.updateById(uploadAvatarUser) == 1;
        if (!updateRet) {
            throw new PetException("用户头像更新失败");
        }

        return fileUrl;
    }

    @Override
    public Boolean realNameAuth(RealNameAuthDTO realNameAuthDTO) {
        if (realNameAuthDTO == null ||
            !StringUtils.hasText(realNameAuthDTO.getRealName()) ||
            !StringUtils.hasText(realNameAuthDTO.getIdCard()) ||
            realNameAuthDTO.getUserId() == null) {
            throw new PetException("用户信息错误，实名认证失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(realNameAuthDTO.getUserId());
        User oldUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, realNameAuthDTO.getUserId())
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (oldUser == null) {
            throw new PetException("指定用户不存在，实名认证失败");
        }

        User existed = userMapper.selectByIdCard(realNameAuthDTO.getIdCard());
        if (existed != null) {
            throw new PetException("当前身份已经认证过一个用户，同一个用户只能认证一个账户");
        }

        User user = new User();
        user.setId(realNameAuthDTO.getUserId());
        user.setRealName(realNameAuthDTO.getRealName());
        user.setIdCard(realNameAuthDTO.getIdCard());

        boolean updateRet = userMapper.updateById(user) == 1;
        if (!updateRet) {
            throw new PetException("实名认证失败");
        }

        return true;
    }

    @Override
    public ProfileReceiptAddressVO getReceiptAddress(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户ID错误，无法获取到地址信息");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("指定用户不存在，获取地址信息失败");
        }
        Address address = addressMapper.selectById(user.getReceiptId());

        return ProfileReceiptAddressVO.builder()
                .receiptId(address.getId()).receiptName(user.getReceiptName())
                .receiptAddress(address.getAddressText())
                .build();
    }

    @Transactional
    @Override
    public ProfileReceiptAddressVO changeReceiptAddress(ProfileReceiptAddressDTO profileReceiptAddressDTO) {
        if (profileReceiptAddressDTO == null) {
            throw new PetException("用户信息错误，修改地址失败");
        }

        Long userId = profileReceiptAddressDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        String receiptAddress = profileReceiptAddressDTO.getReceiptAddress();
        String receiptName = profileReceiptAddressDTO.getReceiptName();

        User oldUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (oldUser == null) {
            throw new PetException("指定用户不存在，修改收货地址失败");
        }

        User user = new User();
        user.setId(userId);
        if (StringUtils.hasText(receiptName)) {
            user.setReceiptName(receiptName);
        }

        Address address = new Address();
        if (StringUtils.hasText(receiptAddress)) {
            address.setAddressText(receiptAddress);
            String mapJson = baiduMapUtil.geographyEncoding(receiptAddress);
            List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
            address.setLatitude(latitudeAndLongitude.get(0));
            address.setLongitude(latitudeAndLongitude.get(1));
            boolean insertRet = addressMapper.insert(address) == 1;
            if (!insertRet) {
                throw new PetException("更新地址失败");
            }
            user.setReceiptId(address.getId());
        }

        boolean updateRet = userMapper.updateById(user) == 1;
        if (!updateRet) {
            throw new PetException("更新用户信息失败");
        }

        return ProfileReceiptAddressVO.builder()
                .receiptId(user.getReceiptId())
                .receiptAddress(receiptAddress)
                .receiptName(receiptName).build();
    }

    @Override
    public Boolean changePassword(ProfilePasswordDTO profilePasswordDTO) {
        if (profilePasswordDTO == null || profilePasswordDTO.getUserId() == null ||
            !StringUtils.hasText(profilePasswordDTO.getOldPassword()) ||
            !StringUtils.hasText(profilePasswordDTO.getNewPassword()) ||
            !StringUtils.hasText(profilePasswordDTO.getConfirmPassword())) {
            throw new PetException("密码信息错误，修改密码失败");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(profilePasswordDTO.getUserId());
        Long userId = profilePasswordDTO.getUserId();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("指定用户不存在，修改密码失败");
        }

        String password = user.getPassword();
        // 加盐匹配
        String oldPassword = profilePasswordDTO.getOldPassword();
        boolean match = passwordEncoder.matches(oldPassword, password);
        if (!match) {
            throw new PetException("旧密码错误，修改密码失败");
        }

        String newPassword = profilePasswordDTO.getNewPassword();
        String confirmPassword = profilePasswordDTO.getConfirmPassword();
        if (newPassword.equals(oldPassword)) {
            throw new PetException("新密码不能与旧密码相同，修改密码失败");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new PetException("两次密码输入不一致，修改密码失败");
        }

        // 将新密码进行加密保存到数据库
        String newEncodedPassword = passwordEncoder.encode(newPassword);
        User newUser = new User();
        newUser.setId(userId);
        newUser.setPassword(newEncodedPassword);

        boolean updateRet = userMapper.updateById(newUser) == 1;
        if (!updateRet) {
            throw new PetException("密码修改失败");
        }

        return true;
    }

    @Override
    public String getPhone(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户ID错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("指定用户不存在，获取电话信息失败");
        }

        return user.getPhone();
    }

    @Override
    public String changePhone(Long userId, String phone) {
        if (userId == null || userId == 0 || !StringUtils.hasText(phone)) {
            throw new PetException("用户信息错误，无法修改手机号");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User oldUser = userMapper.selectByPhone(phone);
        if (oldUser != null) {
            throw new PetException("该手机号已经被占用，无法修改为该手机号");
        }

        User newUser = new User();
        newUser.setId(userId);
        newUser.setPhone(phone);
        boolean updateRet = userMapper.updateById(newUser) == 1;
        if (!updateRet) {
            throw new PetException("手机号修改失败");
        }

        return phone;
    }

    @Override
    public String getEmail(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户ID错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("指定用户不存在，获取电话信息失败");
        }

        return user.getEmail();
    }

    @Override
    public String changeEmail(Long userId, String email) {
        if (userId == null || userId == 0 || !StringUtils.hasText(email)) {
            throw new PetException("用户信息错误，无法修改手机号");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        User oldUser = userMapper.selectByEmail(email);
        if (oldUser != null) {
            throw new PetException("该邮箱已经被占用，无法修改为该邮箱");
        }

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        boolean updateRet = userMapper.updateById(user) == 1;
        if (!updateRet) {
            throw new PetException("邮箱修改失败");
        }

        // 发送通知邮件
        NotifyEmailInfo notifyEmailInfo = new NotifyEmailInfo(email, "修改邮箱成功", "邮箱修改成功，请使用新邮箱进行登录");
        rabbitTemplate.convertAndSend("", Constants.NOTIFY_EMAIL_QUEUE, notifyEmailInfo);

        return email;
    }

    @Transactional
    @Override
    public Boolean deactivateAccount(Long userId) {
        if (userId == null || userId == 0) {
            throw new PetException("用户信息错误，无法注销账号");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .and(wrapper -> wrapper
                    .eq(Order::getStatus, Constants.ORDER_TO_PAY)
                    .or()
                    .eq(Order::getStatus, Constants.ORDER_TO_SIGN)
                ));

        if (!orders.isEmpty()) {
            throw new PetException("存在待支付或者待签收的订单，无法销户");
        }

        User deleteUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (deleteUser == null || deleteUser.getDeleteFlag().equals(Constants.DELETED_FLAG)
            || deleteUser.getStatus().equals(Constants.DEACTIVATED_FLAG)) {
            throw new PetException("用户不存在或者已经被删除或者注销");
        }

        Long receiptId = deleteUser.getReceiptId();
        if (receiptId != null) {
            Address address = new Address();
            address.setId(deleteUser.getReceiptId());
            address.setDeleteFlag(Constants.DELETED_FLAG);
            boolean addressUpdateRet = addressMapper.updateById(address) == 1;
            if (!addressUpdateRet) {
                throw new PetException("地址删除失败");
            }
        }

        // 需要清空用户的购物车
        cartMapper.update(new LambdaUpdateWrapper<Cart>().eq(Cart::getUserId, userId)
                .eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .set(Cart::getDeleteFlag, Constants.DELETED_FLAG));

        User user = new User();
        user.setId(userId);
        user.setStatus(Constants.DEACTIVATED_FLAG);
        boolean userUpdateRet = userMapper.updateById(user) == 1;
        if (!userUpdateRet) {
            throw new PetException("用户删除失败");
        }

        return true;
    }
}
