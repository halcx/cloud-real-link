package net.cloud.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.cloud.model.TrafficDO;

public interface TrafficManager {

    /**
     * 新增流量包
     * @param trafficDO
     * @return
     */
    int add(TrafficDO trafficDO);

    /**
     * 分页查询可用流量包,查询未过期的
     * @param page
     * @return
     */
    IPage<TrafficDO> pageAvailable(int page,int size,Long accountNo);

    /**
     * 查询流量包
     * @param trafficId
     * @param accountNo
     * @return
     */
    TrafficDO findByIdAndAccountNo(Long trafficId,Long accountNo);

    /**
     * 增加某个流量包天使用次数
     * @param currentTrafficId
     * @param accountNo
     * @param dayUseTimes
     * @return
     */
    int addDayUseTimes(long currentTrafficId,Long accountNo,int dayUseTimes);


}
