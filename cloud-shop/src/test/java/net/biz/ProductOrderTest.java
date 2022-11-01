package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.ShopApplication;
import net.cloud.manager.ProductOrderManager;
import net.cloud.model.ProductOrderDO;
import net.cloud.utils.CommonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class)
@Slf4j
public class ProductOrderTest {

    @Autowired
    private ProductOrderManager productOrderManager;

    @Test
    public void testAdd(){
        for (int i = 0; i < 5; i++) {
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                    .outTradeNo(CommonUtil.generateUUID())
                    .payAmount(new BigDecimal(11))
                    .state("NEW")
                    .nickname("wxh")
                    .accountNo(100L)
                    .del(0)
                    .productId(2L).build();
            int add = productOrderManager.add(productOrderDO);
        }
    }

    @Test
    public void testPage(){
        Map<String, Object> page = productOrderManager.page(1, 2, 100L, null);
        log.info(page.toString());
    }
}
