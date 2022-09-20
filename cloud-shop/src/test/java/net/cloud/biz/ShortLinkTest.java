package net.cloud.biz;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import net.cloud.utils.CommonUtil;
import org.junit.Test;


@Slf4j
public class ShortLinkTest {

    @Test
    public void testMurmurHash(){
        for (int i = 0; i < 5; i++) {
            String originalUrl = "https://cloud.net?id="+CommonUtil.generateUUID()+"pwd="+CommonUtil.getStringNumRandom(7);
            long murmur3_32 = Hashing.murmur3_32().hashUnencodedChars(originalUrl).padToLong();
            log.info("url:{} ;murmur3_32:{}",originalUrl,murmur3_32);
        }
    }
}
