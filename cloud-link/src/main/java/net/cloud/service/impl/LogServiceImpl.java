package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.LogTypeEnum;
import net.cloud.model.LogRecord;
import net.cloud.service.LogService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import net.cloud.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LogServiceImpl implements LogService {

    private static final String TOPIC_NAME = "ods_link_visit_topic";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public void recordShortLinkLog(HttpServletRequest request, String shortLinkCode, Long accountNo) {
        //拿到ip、浏览器信息
        String ip = CommonUtil.getIpAddr(request);
        //全部请求头
        Map<String, String> headerMap = CommonUtil.getAllRequestHeader(request);

        HashMap<String, String> availableMap = new HashMap<>();
        availableMap.put("user-agent",headerMap.get("user-agent"));
        //来源，比如淘宝
        availableMap.put("referer",headerMap.get("referer"));
        availableMap.put("accountNo",accountNo.toString());

        LogRecord logRecord = LogRecord.builder()
                //⽇志类型
                .event(LogTypeEnum.SHORT_LINK_TYPE.name())
                //⽇志内容
                .data(availableMap)
                //客户端ip
                .ip(ip)
                //时间时间
                .ts(CommonUtil.getCurrentTimestamp())
                //业务唯⼀id
                .bizId(shortLinkCode).build();
        String jsonLog = JsonUtil.obj2Json(logRecord);
        //打印控制台
        log.info(jsonLog);
        //发送kafka
        kafkaTemplate.send(TOPIC_NAME,jsonLog);
        //存储Mysql 测试数据 TODO
    }
}
