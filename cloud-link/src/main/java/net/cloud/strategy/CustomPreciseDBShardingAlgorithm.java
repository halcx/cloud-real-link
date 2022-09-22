package net.cloud.strategy;

import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 自定义的一个db精准分片策略
 */
public class CustomPreciseDBShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    /**
     *
     * @param collection 数据源集合
     *                   在分库的时候值为所有分片库的集合 databaseNames
     *                   在分表的时候对应分片库总所有分片表的集合 tableNames
     * @param preciseShardingValue 分片属性
     *                             logicTableName 逻辑表
     *                             columnName 分片键（字段）
     *                             value 从 sql 当中解析出来的分片键的值
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        //拿到短链码的第一位,即库位
        String codePrefix = preciseShardingValue.getValue().substring(0, 1);
        //遍历全部的数据源
        for (String targetName : collection) {
            //先拿到全部数据源ds1\ds2\dsa的最后一位(真实配置的ds)
            String targetNameSuffix = targetName.substring(targetName.length() - 1);
            if(codePrefix.equals(targetNameSuffix)){
                //如果一致则返回
                return targetName;
            }
        }
        throw new BizException(BizCodeEnum.DB_ROUTE_NOT_FOUND);
    }
}
