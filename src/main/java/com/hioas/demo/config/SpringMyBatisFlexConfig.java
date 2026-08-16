package com.hioas.demo.config;

import com.hioas.demo.utils.EncryptUtil;
import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.hioas.demo.mapper")
public class SpringMyBatisFlexConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringMyBatisFlexConfig.class);

    @Value("${pay.encrypt.secret-key}")
    private String encryptSecretKey;

    /**
     * 初始化加密密钥,供 EncryptUtil 静态工具使用
     */
    @PostConstruct
    public void initEncryptKey() {
        EncryptUtil.setSecretKey(encryptSecretKey);
        log.info("加密密钥初始化完成");
    }
}
