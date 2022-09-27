package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.DomainTypeEnum;
import net.cloud.manager.DomainManager;
import net.cloud.mapper.DomainMapper;
import net.cloud.model.DomainDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DomainManagerImpl implements DomainManager {

    @Autowired
    private DomainMapper domainMapper;

    @Override
    public DomainDO findById(Long id, Long accountNo) {
        return domainMapper.selectOne(new QueryWrapper<DomainDO>().eq("id",id).eq("account_no",accountNo));
    }

    @Override
    public DomainDO findByDomainTypeAndId(Long id, DomainTypeEnum domainTypeEnum) {
        return domainMapper.selectOne(new QueryWrapper<DomainDO>().eq("id",id).eq("domain_type",domainTypeEnum.name()));
    }

    @Override
    public int addDomain(DomainDO domainDO) {
        return domainMapper.insert(domainDO);
    }

    @Override
    public List<DomainDO> listOfficialDomain() {
        List<DomainDO> domainDOS = domainMapper.selectList(new QueryWrapper<DomainDO>().eq("domain_type", DomainTypeEnum.OFFICIAL.name()));
        return domainDOS;
    }

    @Override
    public List<DomainDO> listCustomDomain(Long accountNo) {
        return domainMapper.selectList(new QueryWrapper<DomainDO>().eq("account_no",accountNo)
                .eq("domain_type", DomainTypeEnum.CUSTOM.name()));
    }
}
