package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.MessageListDTO;
import org.epsda.pets.pojo.vo.ChatMediaVO;
import org.epsda.pets.pojo.vo.CustomerServerInfo;
import org.epsda.pets.pojo.vo.MessageListVO;
import org.epsda.pets.pojo.vo.PageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 15:59
 *
 * @Author: 憨八嘎
 */
public interface CustomerChatService {
    PageVO<MessageListVO> list(MessageListDTO messageListDTO);

    CustomerServerInfo getCustomerServerInfo(Long userId);

    Boolean read(Long userId, Long sendUserId);

    ChatMediaVO uploadMedia(Integer mediaType, MultipartFile file);

    Boolean hasUnread(Long userId);
}
