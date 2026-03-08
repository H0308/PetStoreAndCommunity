package org.epsda.pets.service;

import jakarta.validation.constraints.NotNull;
import org.epsda.pets.pojo.dto.TopicAddDTO;
import org.epsda.pets.pojo.dto.TopicChangeDTO;
import org.epsda.pets.pojo.dto.TopicListDTO;
import org.epsda.pets.pojo.dto.TopicListFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.TopicListVO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 10:04
 *
 * @Author: 憨八嘎
 */
public interface AdminTopicService {
    PageVO<TopicListVO> list(TopicListDTO topicListDTO, TopicListFilterDTO topicListFilterDTO);

    Boolean change(TopicChangeDTO topicChangeDTO);

    Boolean add(TopicAddDTO topicAddDTO);

    Boolean delete(Long topicId, Long userId);

    Boolean batchDelete(List<Long> topicIds, Long userId);
}
