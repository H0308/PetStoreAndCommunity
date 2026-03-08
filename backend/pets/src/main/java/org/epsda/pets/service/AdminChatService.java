package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.HistoryMessageDTO;
import org.epsda.pets.pojo.dto.LatestMessageDTO;
import org.epsda.pets.pojo.vo.ChatUserInfo;
import org.epsda.pets.pojo.vo.LatestMessageListVO;
import org.epsda.pets.pojo.vo.MessageListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 17:32
 *
 * @Author: 憨八嘎
 */
public interface AdminChatService {
    List<LatestMessageListVO> latest(LatestMessageDTO latestMessageDTO);

    PageVO<MessageListVO> history(HistoryMessageDTO historyMessageDTO);

    Boolean read(Long userId, Long sendUserId);

    ChatUserInfo getUserInfo(Long userId, Long sendUserId);
}
