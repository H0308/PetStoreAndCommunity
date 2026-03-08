package org.epsda.pets.service;

import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.SearchListDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SearchListVO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 17:45
 *
 * @Author: 憨八嘎
 */
public interface SearchService {
    PageVO<SearchListVO> search(SearchListDTO searchListDTO);
}
