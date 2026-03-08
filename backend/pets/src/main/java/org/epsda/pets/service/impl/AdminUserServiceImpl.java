package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.common.CommonMessage;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.AddressMapper;
import org.epsda.pets.mapper.CartMapper;
import org.epsda.pets.mapper.UserMapper;
import org.epsda.pets.pojo.Address;
import org.epsda.pets.pojo.Cart;
import org.epsda.pets.pojo.ProductSuper;
import org.epsda.pets.pojo.User;
import org.epsda.pets.pojo.dto.UserDetailChangeDTO;
import org.epsda.pets.pojo.dto.UserDetailListDTO;
import org.epsda.pets.pojo.dto.UserDetailFilterDTO;
import org.epsda.pets.pojo.message.NotifyEmailInfo;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.UserDetailListVO;
import org.epsda.pets.service.AdminUserService;
import org.epsda.pets.service.MessagePushService;
import org.epsda.pets.service.ProfileService;
import org.epsda.pets.utils.BaiduMapUtil;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/02
 * Time: 15:13
 *
 * @Author: 憨八嘎
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private BaiduMapUtil baiduMapUtil;
    @Autowired
    private MessagePushService messagePushService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageVO<UserDetailListVO> list(UserDetailListDTO userDetailListDTO, UserDetailFilterDTO userDetailFilterDTO) {
        if (userDetailListDTO == null) {
            throw new PetException("用户信息错误，获取用户列表失败");
        }

        Long userId = userDetailListDTO.getUserId();
        SecurityUtil.checkAdmin(userId);

        Long currentPage = userDetailListDTO.getCurrentPage();
        Long pageSize = userDetailListDTO.getPageSize();

        Page<User> userPage = new Page<>(currentPage, pageSize);
        // 只不显示被删除的用户，未被删除、未被注销和已注销的用户都正常显示
        // 不显示当前用户
        LambdaQueryWrapper<User> userLambdaQueryWrapper = this.buildSearchCondition(userDetailFilterDTO);
        userLambdaQueryWrapper.eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG).ne(User::getId, userId);
        Page<User> userPages = userMapper.selectPage(userPage, userLambdaQueryWrapper);
        List<UserDetailListVO> userDetailListVOS = new ArrayList<>();
        List<User> users = userPages.getRecords();
        if (!users.isEmpty()) {
            users.forEach(user -> userDetailListVOS.add(this.buildUserDetailListVOFromUser(user)));
        }

        return PageVO.<UserDetailListVO>builder()
                .currentPage(userPages.getCurrent())
                .totalPages(userPages.getPages())
                .totalCount(userPages.getTotal())
                .totalRecords(userDetailListVOS)
                .build();
    }

    @Override
    public Boolean change(UserDetailChangeDTO userDetailChangeDTO) {
        if (userDetailChangeDTO == null) {
            throw new PetException("用户信息不完全，修改信息失败");
        }

        Long userId = userDetailChangeDTO.getUserId();
        SecurityUtil.checkAdmin(userId);
        Long userIdChange = userDetailChangeDTO.getUserIdChange();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userIdChange)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (user == null) {
            throw new PetException("用户不存在或者已经注销，修改失败");
        }

        String phone = userDetailChangeDTO.getPhone();
        String email = userDetailChangeDTO.getEmail();
        String receiptName = userDetailChangeDTO.getReceiptName();
        String receiptAddress = userDetailChangeDTO.getReceiptAddress();
        Long roleId = userDetailChangeDTO.getRoleId();
        User newUser = new User();
        newUser.setId(userIdChange);
        if (StringUtils.hasText(phone)) {
            // 需要用短信验证码或者邮箱验证码校验用户身份
            newUser.setPhone(phone);
        }

        if (StringUtils.hasText((email))) {
            // 需要用短信验证码或者邮箱验证码校验用户身份
            newUser.setEmail(email);
        }

        if (StringUtils.hasText(receiptName)) {
            newUser.setReceiptName(receiptName);
        }

        if (roleId != null) {
            newUser.setRoleId(roleId);
        }

        if (StringUtils.hasText(receiptAddress)) {
            // 删除原有地址
            Address deletedAddress = new Address();
            deletedAddress.setId(user.getReceiptId());
            deletedAddress.setDeleteFlag(Constants.DELETED_FLAG);
            boolean addressUpdateRet = addressMapper.updateById(deletedAddress) == 1;
            if (!addressUpdateRet) {
                throw new PetException("收货地址删除失败");
            }

            // 新增地址
            String mapJson = baiduMapUtil.geographyEncoding(receiptAddress);
            List<String> latitudeAndLongitude = baiduMapUtil.getLatitudeAndLongitude(mapJson);
            Address address = new Address();
            address.setLatitude(latitudeAndLongitude.get(0));
            address.setLongitude(latitudeAndLongitude.get(1));
            address.setAddressText(receiptAddress);
            boolean addressInsertRet = addressMapper.insert(address) == 1;
            if (!addressInsertRet) {
                throw new PetException("收货地址新增失败");
            }
            newUser.setReceiptId(address.getId());
        }

        boolean updateRet = userMapper.updateById(newUser) == 1;
        if (!updateRet) {
            throw new PetException("用户信息修改失败");
        }

        return true;
    }

    @Override
    public Boolean reset(Long userIdChange, Long userId) {
        if (userIdChange == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userIdChange)
                .eq(User::getStatus, Constants.NOT_DEACTIVATE_FLAG)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (user == null) {
            throw new PetException("用户不存在或者已经被销户，无法重置密码");
        }

        User change = new User();
        change.setId(userIdChange);
        String rawPassword = SecurityUtil.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        change.setPassword(encodedPassword);
        boolean updateRet = userMapper.updateById(change) == 1;
        if (!updateRet) {
            throw new PetException("密码重置失败");
        }

        // 可以考虑给用发送邮件通知或者短信提醒
        NotifyEmailInfo notifyEmailInfo =
                new NotifyEmailInfo(user.getEmail(), "密码重置",
                        "尊敬的" + user.getUsername() + "密码重置成功，重置后的密码为：" + rawPassword + "，登录后请及时修改密码");
        rabbitTemplate.convertAndSend("", Constants.NOTIFY_EMAIL_QUEUE, notifyEmailInfo);

        return true;
    }

    @Override
    public Boolean deactivate(Long userIdChange, Long userId) {
        if (userIdChange == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        // 调用用户端的销户操作
        Boolean deactivateFlag = profileService.deactivateAccount(userIdChange);
        if (!deactivateFlag) {
            throw new PetException("用户销户失败");
        }
        return true;
    }

    @Transactional
    @Override
    public Boolean delete(Long userIdChange, Long userId) {
        if (userIdChange == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userIdChange)
                .eq(User::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (user == null) {
            throw new PetException("用户不存在，无法删除");
        }

        Address address = new Address();
        Long receiptId = user.getReceiptId();
        if (receiptId != null) {
            address.setId(receiptId);
            address.setDeleteFlag(Constants.DELETED_FLAG);
            boolean addressUpdateRet = addressMapper.updateById(address) == 1;
            if (!addressUpdateRet) {
                throw new PetException("地址删除失败");
            }
        }

        // 需要清空用户的购物车
        cartMapper.update(new LambdaUpdateWrapper<Cart>().eq(Cart::getUserId, userIdChange)
                .eq(Cart::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .set(Cart::getDeleteFlag, Constants.DELETED_FLAG));

        User change = new User();
        change.setId(userIdChange);
        change.setDeleteFlag(Constants.DELETED_FLAG);
        boolean updateRet = userMapper.updateById(change) == 1;
        if (!updateRet) {
            throw new PetException("用户删除失败");
        }

        return true;
    }

    @Transactional
    @Override
    public Boolean batchDelete(List<Long> userIdChanges, Long userId) {
        if (userIdChanges == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        for (Long userIdChange : userIdChanges) {
            Boolean delete = this.delete(userIdChange, userId);
            if (!delete) {
                throw new PetException("存在用户删除失败，终止删除");
            }
        }

        return true;
    }

    @Override
    public Boolean ban(Long userIdChange, Long userId) {
        if (userIdChange == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        User change = new User();
        change.setId(userIdChange);
        change.setBanFlag(Constants.BANNED_FLAG);
        boolean updateRet = userMapper.updateById(change) == 1;
        if (!updateRet) {
            throw new PetException("用户禁言失败");
        }

        // 给用户发送通知
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        commonMessage.setTitle(Constants.BAN_SYSTEM_MESSAGE_TITLE);
        commonMessage.setContent("您的账号因违反规则，已被禁言。如有疑问，请咨询客服");
        commonMessage.setReceiverId(userIdChange);
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.BAN_SYSTEM_MESSAGE);

        return true;
    }

    @Override
    public Boolean batchBan(List<Long> userIdChanges, Long userId) {
        if (userIdChanges == null || userId == null) {
            throw new PetException("用户ID错误，无法禁言");
        }

        SecurityUtil.checkAdmin(userId);

        for (Long userIdChange : userIdChanges) {
            Boolean delete = this.ban(userIdChange, userId);
            if (!delete) {
                throw new PetException("存在用户禁言失败，终止禁言");
            }
        }

        return true;
    }

    @Override
    public Boolean unBan(Long userIdChange, Long userId) {
        if (userIdChange == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        User change = new User();
        change.setId(userIdChange);
        change.setBanFlag(Constants.NOT_BANNED_FLAG);
        boolean updateRet = userMapper.updateById(change) == 1;
        if (!updateRet) {
            throw new PetException("用户取消禁言失败");
        }

        // 给用户发送通知
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setType(Constants.SYSTEM_MESSAGE);
        commonMessage.setTitle(Constants.BAN_SYSTEM_MESSAGE_TITLE);
        commonMessage.setContent("您的账号已解除禁言，请继续遵守社区规则。如有疑问，请咨询客服");
        commonMessage.setReceiverId(userIdChange);
        messagePushService.saveAndSendSystemMessage(commonMessage, Constants.BAN_SYSTEM_MESSAGE);

        return true;
    }

    @Override
    public Boolean batchUnBan(List<Long> userIdChanges, Long userId) {
        if (userIdChanges == null || userId == null) {
            throw new PetException("用户ID错误，无法重置密码");
        }

        SecurityUtil.checkAdmin(userId);

        for (Long userIdChange : userIdChanges) {
            Boolean delete = this.unBan(userIdChange, userId);
            if (!delete) {
                throw new PetException("存在用户取消禁言失败，终止取消禁言");
            }
        }

        return true;
    }

    public LambdaQueryWrapper<User> buildSearchCondition(UserDetailFilterDTO userDetailFilterDTO) {
        String username = userDetailFilterDTO.getUsername();
        String phone = userDetailFilterDTO.getPhone();
        String email = userDetailFilterDTO.getEmail();
        Long roleId = userDetailFilterDTO.getRoleId();
        Integer status = userDetailFilterDTO.getStatus();
        Integer banFlag = userDetailFilterDTO.getBanFlag();
        Integer realNameAuthFlag = userDetailFilterDTO.getRealNameAuthFlag();

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<User>();
            userLambdaQueryWrapper.like(StringUtils.hasText(username), User::getUsername, username)
                    .like(StringUtils.hasText(phone), User::getPhone, phone)
                    .like(StringUtils.hasText(email), User::getEmail, email)
                    .eq(roleId != null, User::getRoleId, roleId)
                    .eq(status != null, User::getStatus, status)
                    .eq(banFlag != null, User::getBanFlag, banFlag);

        if (realNameAuthFlag != null) {
            if (realNameAuthFlag.equals(Constants.REAL_NAME_NOT_AUTHENTICATE_FLAG)) {
                userLambdaQueryWrapper.isNull(User::getRealName)
                        .or()
                        .isNull(User::getIdCard);
            } else if (realNameAuthFlag.equals(Constants.REAL_NAME_AUTHENTICATED_FLAG)) {
                userLambdaQueryWrapper.isNotNull(User::getRealName)
                        .and(wrapper -> wrapper
                            .isNotNull(User::getIdCard));
            }
        }

        return userLambdaQueryWrapper;
    }

    public UserDetailListVO buildUserDetailListVOFromUser(User user) {
        Long userId = user.getId();
        String avatarUrl = user.getAvatarUrl();
        String username = user.getUsername();
        String email = user.getEmail();
        String phone = user.getPhone();
        Integer gender = user.getGender();
        Long receiptId = user.getReceiptId();
        String receiptName = user.getReceiptName();
        Integer status = user.getStatus();
        Integer banFlag = user.getBanFlag();
        Long roleId = user.getRoleId();
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, receiptId)
                .eq(Address::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        String addressText = "";
        if (address != null) {
            addressText = address.getAddressText();
        }

        String realName = user.getRealName();
        String idCard = user.getIdCard();

        return UserDetailListVO.builder()
                .userId(userId).avatarUrl(avatarUrl)
                .username(username).email(email).phone(phone)
                .gender(gender).receiptName(receiptName)
                .receiptAddress(addressText).status(status)
                .banFlag(banFlag).roleId(roleId)
                .realNameAuthFlag(StringUtils.hasText(idCard) && StringUtils.hasText(realName) ?
                        Constants.REAL_NAME_AUTHENTICATED_FLAG :
                        Constants.REAL_NAME_NOT_AUTHENTICATE_FLAG)
                .build();
    }
}
