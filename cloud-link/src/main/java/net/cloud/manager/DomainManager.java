package net.cloud.manager;

import net.cloud.enums.DomainTypeEnum;
import net.cloud.model.DomainDO;

import java.util.List;

public interface DomainManager {
    /**
     * 查找详情
     * @param id
     * @param accountNo
     * @return
     */
    DomainDO findById(Long id,Long accountNo);

    /**
     * 查找详情
     * @param id
     * @param domainTypeEnum
     * @return
     */
    DomainDO findByDomainTypeAndId(Long id, DomainTypeEnum domainTypeEnum);

    /**
     * 新增
     * @param domainDO
     * @return
     */
    int addDomain(DomainDO domainDO);

    /**
     * 列举全部官方域名
     * @return
     */
    List<DomainDO> listOfficialDomain();

    /**
     * 列举用户全部域名
     * @param accountNo
     * @return
     */
    List<DomainDO> listCustomDomain(Long accountNo);
}
