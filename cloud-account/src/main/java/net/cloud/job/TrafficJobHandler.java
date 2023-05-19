package net.cloud.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import net.cloud.service.TrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficJobHandler {

    @Autowired
    private TrafficService trafficService;

    /**
     * 过期流量包处理
     * @param param
     * @return
     */
    @XxlJob(value = "trafficExpiredHandler",init = "init",destroy = "destroy")
    public ReturnT<String> execute(String param){
        log.info("trafficExpiredHandler 任务方法开始执行,删除过期流量包");
        trafficService.deleteExpiredTraffic();
        return ReturnT.SUCCESS;
    }

    private void init(){
        log.info("trafficExpiredHandler xxl-job init");
    }

    private void destroy(){
        log.info("trafficExpiredHandler xxl-job destroy");
    }
}
