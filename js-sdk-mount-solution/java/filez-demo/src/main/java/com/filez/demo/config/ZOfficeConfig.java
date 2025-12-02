package com.filez.demo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ZOffice service configuration class, used to configure properties related to ZOffice service.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zoffice.service")
@ApiModel(description = "ZOffice service configuration class")
public class ZOfficeConfig {

    /**
     * ZOffice service protocol, for example: http or https
     */
    @ApiModelProperty(value = "Protocol type", example = "http")
    private String schema;

    /**
     * ZOffice service host address
     */
    @ApiModelProperty(value = "Deployed on specific host IP or domain name", example = "192.168.0.122")
    private String host;

    /**
     * ZOffice service port number
     */
    @ApiModelProperty(value = "Port number", example = "8001")
    private Integer port;

    /**
     * ZOffice service context path
     */
    @ApiModelProperty(value = "Context path", example = "/docs/app")
    private String context;

    /**
     * Whether to allow cross-origin requests
     */
    @ApiModelProperty(value = "Whether to allow cross-origin requests", example = "true")
    private boolean cors;

    /**
     * ZOffice application configuration
     */
    @ApiModelProperty(value = "Application configuration")
    private App app;

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "zoffice.service.app")
    @ApiModel(description = "ZOffice application configuration")
    public static class App {

        @ApiModelProperty(value = "Application secret", example = "default-salt")
        private String secret;

    }

}
