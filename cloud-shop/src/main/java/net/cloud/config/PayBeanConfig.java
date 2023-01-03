package net.cloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class PayBeanConfig {

    @Autowired
    private WechatPayConfig payConfig;

    public PrivateKey getPrivateKey() throws IOException {
        InputStream inputStream = new ClassPathResource(payConfig.getPrivateKeyPath()
                .replace("classpath:", "")).getInputStream();

        String content = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining(System.lineSeparator()));

        try {
            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                                            .replaceAll("\\s+", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey finalPrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
            return finalPrivateKey;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java 环境不⽀持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("⽆效的密钥格式");
        }
    }
}
