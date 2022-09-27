package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.interceptor.LoginInterceptor;
import net.cloud.manager.DomainManager;
import net.cloud.model.DomainDO;
import net.cloud.service.DomainService;
import net.cloud.vo.DomainVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DomainServiceImpl implements DomainService {

    @Autowired
    private DomainManager domainManager;

    @Override
    public List<DomainVO> listAll() {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        List<DomainDO> customDomainList = domainManager.listCustomDomain(accountNo);
        List<DomainDO> officialDomainList = domainManager.listOfficialDomain();
        customDomainList.addAll(officialDomainList);

        return customDomainList.stream().map(obj -> beanProcess(obj)).collect(Collectors.toList());
    }

    private DomainVO beanProcess(DomainDO domainDO){
        DomainVO domainVO = new DomainVO();
        BeanUtils.copyProperties(domainDO,domainVO);
        return domainVO;
    }
}
