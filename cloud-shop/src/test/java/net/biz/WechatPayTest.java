package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.ShopApplication;
import net.cloud.config.PayBeanConfig;
import net.cloud.manager.ProductOrderManager;
import net.cloud.model.ProductOrderDO;
import net.cloud.utils.CommonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)
@Slf4j
public class WechatPayTest {

    @Autowired
    private PayBeanConfig payBeanConfig;

    @Test
    public void testLoadPrivateKey() throws IOException {
        String algorithm = payBeanConfig.getPrivateKey().getAlgorithm();
        System.out.println(algorithm);
    }
}
