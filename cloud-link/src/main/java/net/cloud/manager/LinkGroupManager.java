package net.cloud.manager;

import net.cloud.model.LinkGroupDO;

import java.util.List;

public interface LinkGroupManager {
    int add(LinkGroupDO linkGroupDO);

    int del(Long groupId, long accountNo);

    LinkGroupDO detail(Long groupId, long accountNo);

    List<LinkGroupDO> listAllGroup(long accountNo);

    int updateById(LinkGroupDO linkGroupDO);
}
