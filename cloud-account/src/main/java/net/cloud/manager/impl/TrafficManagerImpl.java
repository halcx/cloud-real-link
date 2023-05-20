package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import net.cloud.enums.PluginTypeEnum;
import net.cloud.manager.TrafficManager;
import net.cloud.mapper.TrafficMapper;
import net.cloud.model.TrafficDO;
import net.cloud.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


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


    @Override
    public boolean deleteExpiredTraffic() {
        int rows = trafficMapper.delete(new QueryWrapper<TrafficDO>().le("expired_date", new Date()));
        log.info("删除过期流量包，行数:{}",rows);
        return true;
    }

    /**
     * 查找未过期的流量包列表（不一定可用，可能超过使用次数）
     * @param accountNo
     * @return
     */
    @Override
    public List<TrafficDO> selectAvailableTraffics(Long accountNo) {

        String today = TimeUtil.format(new Date(),"yyyy-MM-dd");

        QueryWrapper<TrafficDO> queryWrapper = new QueryWrapper<TrafficDO>();

        queryWrapper.eq("account_no",accountNo);
        queryWrapper.and(wrapper->wrapper.ge("expired_date",today)
                .or().eq("out_trade_no","free_init"));

        return trafficMapper.selectList(queryWrapper);
    }

    /**
     * 增加可用流量包次数
     * @param accountNo
     * @param trafficId
     * @param usedTimes
     * @return
     */
    @Override
    public int addDayUsedTimes(Long accountNo, Long trafficId, Integer usedTimes) {
        return trafficMapper.addDayUsedTimes(accountNo, trafficId, usedTimes);
    }

    @Override
    public int releaseUsedTimes(Long accountNo, Long trafficId, Integer usedTimes) {
        return trafficMapper.releaseUsedTimes(accountNo,trafficId, usedTimes);
    }

    /**
     * 更新流量包，把每天使用量改为0
     * @param accountNo
     * @param unUpdatedTrafficIds
     * @return
     */
    @Override
    public int batchUpdateUsedTimes(Long accountNo, List<Long> unUpdatedTrafficIds) {
        int update = trafficMapper.update(null, new UpdateWrapper<TrafficDO>()
                .eq("account_no", accountNo)
                .in("id", unUpdatedTrafficIds)
                .set("day_used", 0));
        return update;
    }
}
