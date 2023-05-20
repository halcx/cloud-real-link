package net.cloud.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cloud.model.TrafficDO;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseTrafficVO {
    /**
     * 天剩余可用总次数 = 总次数 - 已用
     */
    private Integer dayTotalLeftTimes;

    /**
     * 当前创建短链使用的流量包
     */
    private TrafficDO currentTrafficDO;

    /**
     * 记录没有过期但是没更新的流量包id
     */
    private List<Long> unUpdatedTrafficIds;
}
