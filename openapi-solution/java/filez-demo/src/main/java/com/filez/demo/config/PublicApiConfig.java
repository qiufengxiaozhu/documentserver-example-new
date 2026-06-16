package com.filez.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PublicAPI 配置类
 * 对应 zoffice.yml 中的 zoffice.service.publicapi 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zoffice.service.publicapi")
public class PublicApiConfig {

    /** publicApi 的 appId，固定值 publicApi */
    private String appId = "publicApi";

    /** publicApi 的密钥，需要与 luoshu-server 管理控制台中配置的 secret 一致 */
    private String secret = "default-salt";

    /** publicApi 的接口路径前缀 */
    private String context = "/publicapi/v1";
}
