package net.cloud.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShardingTableConfig {
    /**
     * 存储已经启用的表位
     */
    private static final List<String> tableSuffixList = new ArrayList<>();

    private static Random random = new Random();

    //配置启用哪些库的前缀
    static {
        tableSuffixList.add("0");
        tableSuffixList.add("a");
    }

    /**
     * 获取随机的表后缀
     * @return
     */
    public static String getRandomTableSuffix(){
        int index = random.nextInt(tableSuffixList.size());
        return tableSuffixList.get(index);
    }
}
