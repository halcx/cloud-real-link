package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.ShortLinkStateEnum;
import net.cloud.manager.GroupCodeMappingManager;
import net.cloud.manager.ShortLinkManager;
import net.cloud.mapper.GroupCodeMappingMapper;
import net.cloud.model.GroupCodeMappingDO;
import net.cloud.vo.GroupCodeMappingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GroupCodeMappingManagerImpl implements GroupCodeMappingManager {

    @Autowired
    private GroupCodeMappingMapper groupCodeMappingMapper;

    @Override
    public GroupCodeMappingDO findByGroupIdAndMappingId(Long mappingId, Long accountNo, Long groupId) {
        GroupCodeMappingDO groupCodeMappingDO = groupCodeMappingMapper.selectOne(new QueryWrapper<GroupCodeMappingDO>()
                .eq("id", mappingId)
                //分库id
                .eq("account_no", accountNo)
                //分表id
                .eq("group_id", groupId).eq("del",0));
        return groupCodeMappingDO;
    }

    @Override
    public int add(GroupCodeMappingDO groupCodeMappingDO) {
        return groupCodeMappingMapper.insert(groupCodeMappingDO);
    }

    /**
     * 逻辑删除
     * @param shortLinkCode
     * @param accountNo 分片
     * @param groupId 分片
     * @return
     */
    @Override
    public int del(String shortLinkCode, Long accountNo, Long groupId) {
        int rows = groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
                .eq("code", shortLinkCode)
                //分库id
                .eq("account_no", accountNo)
                //分表id
                .eq("group_id", groupId)
                .set("del", 1));
        return rows;
    }

    @Override
    public Map<String, Object> pageShortLinkByGroupId(Integer page, Integer size, Long accountNo, Long groupId) {
        Page<GroupCodeMappingDO> pageInfo = new Page<>(page,size);
        Page<GroupCodeMappingDO> groupCodeMappingDOPage = groupCodeMappingMapper.selectPage(pageInfo, new QueryWrapper<GroupCodeMappingDO>()
                //分库id
                .eq("account_no", accountNo)
                //分表id
                .eq("group_id", groupId).eq("del",0));
        Map<String,Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record",groupCodeMappingDOPage.getTotal());
        pageMap.put("total_page",groupCodeMappingDOPage.getPages());
        //数据要转换成vo类
        pageMap.put("current_data",groupCodeMappingDOPage.getRecords()
                .stream()
                .map(obj -> beanProcess(obj))
                .collect(Collectors.toList())
        );
        return pageMap;
    }

    private GroupCodeMappingVO beanProcess(GroupCodeMappingDO groupCodeMappingDO) {
        GroupCodeMappingVO groupCodeMappingVO = new GroupCodeMappingVO();
        BeanUtils.copyProperties(groupCodeMappingDO,groupCodeMappingVO);
        return groupCodeMappingVO;
    }

    @Override
    public int updateGroupCodeMappingState(Long accountNo, Long groupId, String shortLinkCode, ShortLinkStateEnum shortLinkStateEnum) {
        int rows = groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
                .eq("code", shortLinkCode)
                //分库id
                .eq("account_no", accountNo)
                //分表id
                .eq("group_id", groupId)
                        .eq("del",0)
                .set("state", shortLinkStateEnum.name()));
        return rows;
    }

    @Override
    public GroupCodeMappingDO findByCodeAndGroupId(String shortLinkCode, Long id, Long accountNo) {
        GroupCodeMappingDO groupCodeMappingDO = groupCodeMappingMapper.selectOne(new QueryWrapper<GroupCodeMappingDO>()
                .eq("code", shortLinkCode)
                //分库id
                .eq("account_no", accountNo)
                //分表id
                .eq("group_id", id).eq("del",0));
        return groupCodeMappingDO;
    }

    @Override
    public int update(GroupCodeMappingDO groupCodeMappingDO) {
        int rows = groupCodeMappingMapper.update(null, new UpdateWrapper<GroupCodeMappingDO>()
                .eq("id", groupCodeMappingDO.getId())
                //分库
                .eq("account_no", groupCodeMappingDO.getAccountNo())
                //分表
                .eq("group_id", groupCodeMappingDO.getGroupId())
                .eq("del", 0)
                .set("title", groupCodeMappingDO.getTitle())
                .set("domain", groupCodeMappingDO.getDomain())
        );
        return rows;
    }
}
