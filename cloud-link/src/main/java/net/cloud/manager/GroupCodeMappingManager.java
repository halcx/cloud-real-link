package net.cloud.manager;

import net.cloud.enums.ShortLinkStateEnum;
import net.cloud.model.GroupCodeMappingDO;

import java.util.Map;

public interface GroupCodeMappingManager {
    /**
     * 查找详情
     * @param mappingId
     * @param accountNo 分片键
     * @param groupId 分片键
     * @return
     */
    GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId,Long accountNo,Long groupId);

    /**
     * 新增
     * @param groupCodeMappingDO
     * @return
     */
    int add(GroupCodeMappingDO groupCodeMappingDO);

    /**
     * 根据短链码删除
     * @param shortLinkCode
     * @param accountNo 分片
     * @param groupId 分片
     * @return
     */
    int del(String shortLinkCode,Long accountNo,Long groupId);

    /**
     * 分页查找
     * @param page
     * @param size
     * @param accountNo
     * @param groupId
     * @return
     */
    Map<String,Object> pageShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId);

    /**
     * 更新短链码状态
     * @param accountNo
     * @param groupId
     * @param shortLinkCode
     * @param shortLinkStateEnum
     * @return
     */
    int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode, ShortLinkStateEnum shortLinkStateEnum);
}
