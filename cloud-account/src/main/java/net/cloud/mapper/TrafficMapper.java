package net.cloud.mapper;

import net.cloud.model.TrafficDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Wxh
 * @since 2022-09-07
 */
public interface TrafficMapper extends BaseMapper<TrafficDO> {

    /**
     * 给某个流量包增加甜使用
     * @param accountNo
     * @param trafficId
     * @param usedTimes
     * @return
     */
    int addDayUsedTimes(@Param("accountNo") Long accountNo, @Param("trafficId") Long trafficId, @Param("usedTimes") Integer usedTimes);

    /**
     * 恢复流量包到每日初始流量
     * @param accountNo
     * @param trafficId
     * @param usedTimes
     * @return
     */
    int releaseUsedTimes(@Param("accountNo") Long accountNo,@Param("trafficId") Long trafficId,@Param("usedTimes") Integer usedTimes);
}
