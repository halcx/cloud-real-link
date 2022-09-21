package net.cloud.service.impl;

import net.cloud.controller.request.LinkGroupAddRequest;
import net.cloud.controller.request.LinkGroupUpdateRequest;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.LinkGroupManager;
import net.cloud.model.LinkGroupDO;
import net.cloud.service.LinkGroupService;
import net.cloud.vo.LinkGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LinkGroupServiceImpl implements LinkGroupService {

    @Autowired
    private LinkGroupManager linkGroupManager;

    /**
     * 增加分组
     * @param addRequest
     * @return
     */
    @Override
    public int add(LinkGroupAddRequest addRequest) {

        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setTitle(addRequest.getTitle());
        linkGroupDO.setAccountNo(accountNo);

        int rows = linkGroupManager.add(linkGroupDO);
        return rows;
    }

    /**
     * 删除分组
     * @param groupId
     * @return
     */
    @Override
    public int del(Long groupId) {
        //删除的时候一定要获取已经登陆的account_no，防止越权
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();

        int rows = linkGroupManager.del(groupId,accountNo);
        return rows;
    }

    /**
     * 查看详情
     * @param groupId
     * @return
     */
    @Override
    public LinkGroupVO detail(Long groupId) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = linkGroupManager.detail(groupId,accountNo);
        LinkGroupVO linkGroupVO = new LinkGroupVO();

        // 还有mapStruct
        BeanUtils.copyProperties(linkGroupDO,linkGroupVO);
        return linkGroupVO;
    }

    /**
     * 列出用户全部分组
     * @return
     */
    @Override
    public List<LinkGroupVO> listAllGroup() {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<LinkGroupDO> linkGroupDOList = linkGroupManager.listAllGroup(accountNo);
        List<LinkGroupVO> groupVOList = linkGroupDOList.stream().map(obj -> {
            LinkGroupVO linkGroupVO = new LinkGroupVO();
            BeanUtils.copyProperties(obj, linkGroupVO);
            return linkGroupVO;
        }).collect(Collectors.toList());
        return groupVOList;
    }

    /**
     * 更新组名
     * @param request
     * @return
     */
    @Override
    public int updateById(LinkGroupUpdateRequest request) {
        long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        LinkGroupDO linkGroupDO = new LinkGroupDO();
        linkGroupDO.setAccountNo(accountNo);
        linkGroupDO.setTitle(request.getTitle());
        linkGroupDO.setId(request.getId());
        int rows = linkGroupManager.updateById(linkGroupDO);
        return rows;
    }
}
