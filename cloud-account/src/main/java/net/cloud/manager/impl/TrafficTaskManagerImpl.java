package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.TrafficTaskManager;
import net.cloud.mapper.TrafficTaskMapper;
import net.cloud.model.TrafficTaskDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrafficTaskManagerImpl implements TrafficTaskManager {
    @Autowired
    private TrafficTaskMapper trafficTaskMapper;

    @Override
    public int add(TrafficTaskDO trafficTaskDO) {
        return trafficTaskMapper.insert(trafficTaskDO);
    }

    @Override
    public TrafficTaskDO findByIdAndAccountNo(Long id, Long accountNo) {
        TrafficTaskDO taskDO = trafficTaskMapper.selectOne(new QueryWrapper<TrafficTaskDO>()
                        .eq("id", id).eq("account_no", accountNo));
        return taskDO;
    }

    @Override
    public int deleteByIdAndAccountNo(Long id, Long accountNo) {
        return trafficTaskMapper.delete(new QueryWrapper<TrafficTaskDO>()
                .eq("id", id).eq("account_no", accountNo));
    }
}
