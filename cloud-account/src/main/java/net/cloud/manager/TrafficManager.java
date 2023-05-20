package net.cloud.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.cloud.model.TrafficDO;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

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

    boolean deleteExpiredTraffic();

    /**
     * 查找可⽤的短链流包(未过期),包括免费流包
     * @param accountNo
     * @return
     */
    List<TrafficDO> selectAvailableTraffics( Long accountNo);

    /**
     * 给某个流包增加使⽤次数
     *
     * @param accountNo
     * @param usedTimes
     * @return
     */
    int addDayUsedTimes(Long accountNo, Long trafficId, Integer usedTimes) ;

    /**
     * 恢复流包使⽤当天次数
     * @param accountNo
     * @param trafficId
     * @param usedTimes
     */
    int releaseUsedTimes(Long accountNo, Long trafficId, Integer usedTimes, String useDateStr);

    /**
     * 批更新流包使⽤次数为0
     * @param accountNo
     * @param unUpdatedTrafficIds
     */
    int batchUpdateUsedTimes(Long accountNo, List<Long> unUpdatedTrafficIds);
}
