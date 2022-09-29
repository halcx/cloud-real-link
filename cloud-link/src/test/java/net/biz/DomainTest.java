package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.LinkApplication;
import net.cloud.manager.DomainManager;
import net.cloud.model.DomainDO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkApplication.class)
@Slf4j
public class DomainTest {

    @Autowired
    private DomainManager domainManager;

    /**
     * 没有额外配置domain的时候，会自动到第一个数据源ds0去找
     */
    @Test
    public void testDomain(){
        List<DomainDO> domainDOS = domainManager.listOfficialDomain();
        log.info(domainDOS.toString());
    }
}
