package org.epsda.pets.controller.user.search;

import io.swagger.v3.oas.annotations.Operation;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.SearchListDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.SearchListVO;
import org.epsda.pets.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/09
 * Time: 17:29
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/user/search")
@PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @Operation(summary = "根据商品名称或者帖子标题进行全站搜索")
    @PostMapping("")
    public ResultWrapper<PageVO<SearchListVO>> search(@Validated @RequestBody SearchListDTO searchListDTO) {
        return ResultWrapper.normal(searchService.search(searchListDTO));
    }
}
