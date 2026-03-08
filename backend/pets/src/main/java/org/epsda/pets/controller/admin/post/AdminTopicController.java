package org.epsda.pets.controller.admin.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.pojo.dto.TopicAddDTO;
import org.epsda.pets.pojo.dto.TopicChangeDTO;
import org.epsda.pets.pojo.dto.TopicListFilterDTO;
import org.epsda.pets.pojo.dto.TopicListWithFilterDTO;
import org.epsda.pets.pojo.vo.PageVO;
import org.epsda.pets.pojo.vo.TopicListVO;
import org.epsda.pets.service.AdminTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/01/07
 * Time: 9:36
 *
 * @Author: 憨八嘎
 */
@RestController
@RequestMapping("/api/admin/topic")
public class AdminTopicController {

    @Autowired
    private AdminTopicService adminTopicService;

    // 获取所有话题
    @Operation(summary = "获取所有话题")
    @PostMapping("/list")
    public ResultWrapper<PageVO<TopicListVO>> list(@Validated @RequestBody TopicListWithFilterDTO topicListWithFilterDTO) {
        return ResultWrapper.normal(adminTopicService.list(topicListWithFilterDTO.getTopicListDTO(),
                topicListWithFilterDTO.getTopicListFilterDTO()));
    }

    // 修改话题
    @Operation(summary = "修改话题")
    @PostMapping("change")
    public ResultWrapper<Boolean> change(@Validated @RequestBody TopicChangeDTO topicChangeDTO) {
        return ResultWrapper.normal(adminTopicService.change(topicChangeDTO));
    }

    // 新增话题
    @Operation(summary = "新增话题")
    @PostMapping("/add")
    public ResultWrapper<Boolean> add(@Validated @RequestBody TopicAddDTO topicAddDTO) {
        return ResultWrapper.normal(adminTopicService.add(topicAddDTO));
    }

    // 删除话题
    @Operation(summary = "删除话题")
    @PostMapping("/delete")
    public ResultWrapper<Boolean> delete(@NotNull @RequestParam("topicId") Long topicId,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminTopicService.delete(topicId, userId));
    }

    // 批量删除话题
    @Operation(summary = "批量删除话题")
    @PostMapping("/batchDelete")
    public ResultWrapper<Boolean> batchDelete(@NotNull @RequestParam("topicIds") List<Long> topicIds,
                                         @NotNull @RequestParam("userId") Long userId) {
        return ResultWrapper.normal(adminTopicService.batchDelete(topicIds, userId));
    }
}
