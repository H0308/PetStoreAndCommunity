package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.ChatHistoryDTO;
import org.epsda.pets.pojo.dto.ChatListDTO;
import org.epsda.pets.pojo.vo.ChatCreateVO;
import org.epsda.pets.pojo.vo.ChatHistoryVO;
import org.epsda.pets.pojo.vo.ChatListVO;
import org.epsda.pets.pojo.vo.PageVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/03/03
 * Time: 21:03
 *
 * @Author: 憨八嘎
 */
public interface AiChatService {
    PageVO<ChatListVO> list(ChatListDTO chatListDTO);

    ChatCreateVO create(Long userId);

    String title(Long chatId, String firstMessage);

    PageVO<ChatHistoryVO> history(ChatHistoryDTO chatHistoryDTO);

    Boolean delete(Long chatId, Long userId);
}
