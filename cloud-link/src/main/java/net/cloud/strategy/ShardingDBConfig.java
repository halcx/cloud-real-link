package net.cloud.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShardingDBConfig {
    /**
     * 存储已经启用的库表位
     */
    private static final List<String> dbPrefixList = new ArrayList<>();

    private static Random random = new Random();

    //配置启用哪些库的前缀
    static {
        dbPrefixList.add("0");
        dbPrefixList.add("1");
        dbPrefixList.add("a");
    }

    /**
     * 获取随机的库前缀
     * @return
     */
    public static String getRandomDBPrefix(){
        int index = random.nextInt(dbPrefixList.size());
        return dbPrefixList.get(index);
    }
}
