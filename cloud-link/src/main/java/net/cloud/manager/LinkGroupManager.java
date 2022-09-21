package net.cloud.manager;

import net.cloud.model.LinkGroupDO;

public interface LinkGroupManager {
    int add(LinkGroupDO linkGroupDO);

    int del(Long groupId, long accountNo);
}
