package net.biz;

import lombok.extern.slf4j.Slf4j;
import net.cloud.strategy.ShardingDBConfig;
import org.junit.Test;

@Slf4j
public class CommonTest {
    @Test
    public void testRandomDB(){
        for (int i = 0; i < 20; i++) {
            log.info(ShardingDBConfig.getRandomDBPrefix());
        }
    }
}
