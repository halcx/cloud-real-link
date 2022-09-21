package net.cloud.controller;


import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.LinkGroupAddRequest;
import net.cloud.controller.request.LinkGroupUpdateRequest;
import net.cloud.enums.BizCodeEnum;
import net.cloud.service.LinkGroupService;
import net.cloud.utils.JsonData;
import net.cloud.vo.LinkGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Wxh
 * @since 2022-09-21
 */
@RestController
@RequestMapping("/api/group/v1")
@Slf4j
public class LinkGroupController {

    @Autowired
    private LinkGroupService linkGroupService;

    /**
     * 创建分组
     * @param addRequest
     * @return
     */
    @PostMapping("/add")
    public JsonData add(@RequestBody LinkGroupAddRequest addRequest){
        int rows = linkGroupService.add(addRequest);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_ADD_FAILED);
    }

    @DeleteMapping("/del/{group_id}")
    public JsonData del(@PathVariable("group_id")Long groupId){
        int rows = linkGroupService.del(groupId);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_NOT_EXIST);
    }

    /**
     * 根据id找详情
     * @param groupId
     * @return
     */
    @GetMapping("/detail/{group_id}")
    public JsonData detail(@PathVariable("group_id") Long groupId){
        LinkGroupVO linkGroupVO = linkGroupService.detail(groupId);
        return JsonData.buildSuccess(linkGroupVO);
    }

    /**
     * 列出用户全部分组
     * @return
     */
    @GetMapping("list")
    public JsonData findUserAllLinkGroup(){
        List<LinkGroupVO> linkGroupVOList = linkGroupService.listAllGroup();
        return JsonData.buildSuccess(linkGroupVOList);
    }

    /**
     * 列出用户全部分组
     * @return
     */
    @PutMapping("update")
    public JsonData update(@RequestBody LinkGroupUpdateRequest request){
        int rows = linkGroupService.updateById(request);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);
    }
}

