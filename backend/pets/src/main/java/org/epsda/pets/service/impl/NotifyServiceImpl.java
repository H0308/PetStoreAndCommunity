package org.epsda.pets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.epsda.pets.mapper.NotifyMapper;
import org.epsda.pets.pojo.Notify;
import org.epsda.pets.pojo.dto.NotifyListDTO;
import org.epsda.pets.pojo.vo.NotifyListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.service.NotifyService;
import org.epsda.pets.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/14
 * Time: 20:18
 *
 * @Author: 憨八嘎
 */
@Service
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public PageVO<NotifyListVO> list(NotifyListDTO notifyListDTO) {
        if (notifyListDTO == null) {
            throw new PetException("通知信息错误，获取通知列表失败");
        }

        Long currentPage = notifyListDTO.getCurrentPage();
        Long pageSize = notifyListDTO.getPageSize();
        Integer type = notifyListDTO.getType();
        Long userId = notifyListDTO.getUserId();
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Page<Notify> notifyPage = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Notify> notifyLambdaQueryWrapper = new LambdaQueryWrapper<Notify>()
                .eq(Notify::getReceiveUserId, userId)
                .eq(Notify::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .orderByDesc(Notify::getCreateTime);
        if (type != null) {
            notifyLambdaQueryWrapper.eq(Notify::getType, type);
        }

        Page<Notify> notifyPages = notifyMapper.selectPage(notifyPage, notifyLambdaQueryWrapper);
        List<Notify> records = notifyPages.getRecords();
        List<NotifyListVO> notifyListVOS = new ArrayList<>();
        if (!records.isEmpty()) {
            records.forEach(record -> notifyListVOS.add(this.buildNotifyListVOFromNotify(record)));
        }

        return PageVO.<NotifyListVO>builder()
                .currentPage(notifyPages.getCurrent())
                .totalPages(notifyPages.getPages())
                .totalCount(notifyPages.getTotal())
                .totalRecords(notifyListVOS)
                .build();
    }

    @Override
    public Boolean read(Long userId, Long notifyId) {
        if (userId == null || notifyId == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        Notify notify = notifyMapper.selectOne(new LambdaQueryWrapper<Notify>()
                .eq(Notify::getReceiveUserId, userId)
                .eq(Notify::getId, notifyId)
                .eq(Notify::getStatus, Constants.UNREAD_FLAG)
                .eq(Notify::getDeleteFlag, Constants.NOT_DELETED_FLAG));
        if (notify == null) {
            throw new PetException("消息不存在或者已经已读");
        }

        Notify readNotify = new Notify();
        readNotify.setId(notifyId);
        readNotify.setStatus(Constants.READ_FLAG);
        boolean updateRet = notifyMapper.updateById(readNotify) == 1;
        if (!updateRet) {
            throw new PetException("读消息失败");
        }

        return true;
    }

    @Override
    public Boolean readAll(Long userId, List<Long> notifyIds) {
        if (userId == null || notifyIds == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        if (notifyIds.isEmpty()) {
            throw new PetException("没有需要读的消息");
        }

        int update = notifyMapper.update(new LambdaUpdateWrapper<Notify>()
                .eq(Notify::getReceiveUserId, userId)
                .eq(Notify::getDeleteFlag, Constants.NOT_DELETED_FLAG)
                .in(Notify::getId, notifyIds)
                .set(Notify::getStatus, Constants.READ_FLAG));

        if (update != notifyIds.size()) {
            throw new PetException("存在消息已读失败");
        }

        return true;
    }

    @Override
    public Long unreadCount(Long userId) {
        if (userId == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        return notifyMapper.selectCount(new LambdaQueryWrapper<Notify>()
                .eq(Notify::getReceiveUserId, userId)
                .eq(Notify::getStatus, Constants.UNREAD_FLAG)
                .eq(Notify::getDeleteFlag, Constants.NOT_DELETED_FLAG));
    }

    @Override
    public Boolean delete(Long userId, Long notifyId) {
        if (userId == null || notifyId == null) {
            throw new PetException("用户信息错误");
        }
        // 水平越权校验
        SecurityUtil.checkHorizontalOversteppedForNormal(userId);
        // 先已读消息
        Notify notify = notifyMapper.selectOne(new LambdaQueryWrapper<Notify>()
                .eq(Notify::getReceiveUserId, userId)
                .eq(Notify::getId, notifyId)
                .eq(Notify::getStatus, Constants.UNREAD_FLAG)
                .eq(Notify::getDeleteFlag, Constants.NOT_DELETED_FLAG));

        if (notify != null) {
            Notify readNotify = new Notify();
            readNotify.setId(notifyId);
            readNotify.setStatus(Constants.READ_FLAG);
            boolean updateRet = notifyMapper.updateById(readNotify) == 1;
            if (!updateRet) {
                throw new PetException("读消息失败");
            }
        }

        // 再删除
        Notify deleteNotify = new Notify();
        deleteNotify.setId(notifyId);
        deleteNotify.setDeleteFlag(Constants.DELETED_FLAG);
        boolean updateRet = notifyMapper.updateById(deleteNotify) == 1;
        if (!updateRet) {
            throw new PetException("删除消息失败");
        }

        return true;
    }

    private NotifyListVO buildNotifyListVOFromNotify(Notify notify) {
        if (notify == null) {
            return null;
        }

        return NotifyListVO.builder()
                .notifyId(notify.getId()).title(notify.getTitle())
                .content(notify.getContent()).type(notify.getType())
                .status(notify.getStatus()).createTime(notify.getCreateTime())
                .build();
    }
}
