package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.TrafficManager;
import net.cloud.mapper.TrafficMapper;
import net.cloud.model.TrafficDO;
import net.cloud.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
@Slf4j
public class TrafficManagerImpl implements TrafficManager {

    @Autowired
    private TrafficMapper trafficMapper;

    @Override
    public int add(TrafficDO trafficDO) {
        return trafficMapper.insert(trafficDO);
    }

    @Override
    public IPage<TrafficDO> pageAvailable(int page, int size, Long accountNo) {
        Page<TrafficDO> pageInfo = new Page<>(page,size);
        //因为流量包会过期，所以要构造个过期时间判断
        String today = TimeUtil.format(new Date(),"yyyy-MM-dd");
        Page<TrafficDO> trafficDOPage = trafficMapper.selectPage(pageInfo, new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo).ge("expired_date", today).orderByDesc("gmt_create"));
        return trafficDOPage;
    }

    @Override
    public TrafficDO findByIdAndAccountNo(Long trafficId, Long accountNo) {
        TrafficDO trafficDO = trafficMapper.selectOne(new QueryWrapper<TrafficDO>()
                .eq("account_no", accountNo).eq("id", trafficId));
        return trafficDO;
    }

    /**
     * 给某个流量包增加天使用次数
     * @param currentTrafficId
     * @param accountNo
     * @param dayUseTimes
     * @return
     */
    @Override
    public int addDayUseTimes(long currentTrafficId, Long accountNo, int dayUseTimes) {
        return trafficMapper.update(null,new UpdateWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .eq("id",currentTrafficId).set("day_used",dayUseTimes));
    }
}
