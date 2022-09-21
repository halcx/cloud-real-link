package net.cloud.controller;


import net.cloud.controller.request.LinkGroupAddRequest;
import net.cloud.enums.BizCodeEnum;
import net.cloud.service.LinkGroupService;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

