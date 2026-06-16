package com.filez.demo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * ZOffice服务的配置类，用于配置与ZOffice服务相关的属性。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zoffice.service")
@ApiModel(description = "ZOffice服务的配置类")
public class ZOfficeConfig {

    /**
     * ZOffice服务的协议，例如：http或https
     */
    @ApiModelProperty(value = "协议类型", example = "http")
    private String schema;

    /**
     * ZOffice服务的主机地址
     */
    @ApiModelProperty(value = "部署于具体的主机IP或域名", example = "192.168.0.122")
    private String host;

    /**
     * ZOffice服务的端口号
     */
    @ApiModelProperty(value = "端口号", example = "8001")
    private Integer port;

    /**
     * ZOffice服务的上下文路径
     */
    @ApiModelProperty(value = "上下文路径", example = "/docs/app/driver-callback")
    private String context;

    /**
     * 是否允许跨域请求
     */
    @ApiModelProperty(value = "是否允许跨域请求", example = "true")
    private boolean cors;

    /**
     * ZOffice应用配置
     */
    @ApiModelProperty(value = "应用配置")
    private App app;

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "zoffice.service.app")
    @ApiModel(description = "ZOffice应用配置")
    public static class App {

        @ApiModelProperty(value = "应用密钥", example = "default-salt")
        private String secret;

        @ApiModelProperty(value = "前端集成配置")
        private FeIntegration feIntegration;

        @Data
        @Component
        @ConfigurationProperties(prefix = "zoffice.service.app.fe-integration")
        @ApiModel(description = "前端集成配置")
        public static class FeIntegration {

            @ApiModelProperty(value = "是否开启前端集成", example = "true")
            private boolean enable;
        }
    }
}
