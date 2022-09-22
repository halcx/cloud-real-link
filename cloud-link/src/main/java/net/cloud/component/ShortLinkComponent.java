package net.cloud.component;

import net.cloud.strategy.ShardingDBConfig;
import net.cloud.strategy.ShardingTableConfig;
import net.cloud.utils.CommonUtil;
import org.springframework.stereotype.Component;


@Component
public class ShortLinkComponent {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String createShortLinkCode(String param){
        long murmurHash32 = CommonUtil.murmurHash32(param);
        //转换为62进制
        String code = encodeToBase62(murmurHash32);

        //拼接前缀和后缀
        String shortLinkCode = ShardingDBConfig.getRandomDBPrefix() + code + ShardingTableConfig.getRandomTableSuffix();
        return shortLinkCode;
    }

    /**
     * 10进制转为62进制
     * @param num
     * @return
     */
    private String encodeToBase62(long num){
        //StringBuffer 线程安全 StringBuilder线程不安全
        StringBuffer sb = new StringBuffer();
        do{
            int i = (int) (num % 62);
            sb.append(CHARS.charAt(i));
            num /= 62;
        }while (num>0);

        String value = sb.reverse().toString();
        return value;
    }
}
