package net.cloud.strategy;

import net.cloud.enums.BizCodeEnum;
import net.cloud.exception.BizException;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 自定义的一个table精准分片策略
 */
public class CustomPreciseTableShardingAlgorithm implements PreciseShardingAlgorithm<String> {
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
        //获取逻辑表
        String targetName = collection.iterator().next();
        //短链码
        String value = preciseShardingValue.getValue();
        //获取短链码最后一位
        String codeSuffix = value.substring(value.length() - 1);
        //拼接Actual table
        return targetName+"_"+codeSuffix;
    }
}
