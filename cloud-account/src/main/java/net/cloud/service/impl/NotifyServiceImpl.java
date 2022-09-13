package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.service.NotifyService;
import net.cloud.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 用于测试
     */
    @Override
    @Async
    public void testSend() {
        long beginTime = CommonUtil.getCurrentTimestamp();
        ResponseEntity<String> response = restTemplate.getForEntity("http://old.xdclass.net", String.class);
        String body = response.getBody();
        long endTime = CommonUtil.getCurrentTimestamp();
        log.info("耗时:{}",endTime-beginTime);
        log.info(body);
    }
}
