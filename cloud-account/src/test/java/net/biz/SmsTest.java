package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.AccountApplication;
import net.cloud.component.SmsComponent;
import net.cloud.config.SmsConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class)
@Slf4j
public class SmsTest {

    @Autowired
    private SmsComponent smsComponent;

    @Autowired
    private SmsConfig smsConfig;

    @Test
    public void testSendSms(){
        smsComponent.send("18211905092",smsConfig.getTemplateId(),"666888");
    }
}
