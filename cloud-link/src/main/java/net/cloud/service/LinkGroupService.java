package net.cloud.service;

import net.cloud.controller.request.LinkGroupAddRequest;
import net.cloud.controller.request.LinkGroupUpdateRequest;
import net.cloud.vo.LinkGroupVO;

import java.util.List;

public interface LinkGroupService {
    /**
     * 新增分组
     * @param addRequest
     * @return
     */
    int add(LinkGroupAddRequest addRequest);

    /**
     * 删除分组
     * @param groupId
     * @return
     */
    int del(Long groupId);

    /**
     * 详情
     * @param groupId
     * @return
     */
    LinkGroupVO detail(Long groupId);

    /**
     * 列出用户全部分组
     * @return
     */
    List<LinkGroupVO> listAllGroup();

    /**
     * 更新组名
     * @param request
     * @return
     */
    int updateById(LinkGroupUpdateRequest request);
}
