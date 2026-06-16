package com.filez.demo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 三方业务系统的配置信息
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "demo")
@ApiModel(value = "DemoConfig", description = "业务系统的配置信息")
public class DemoConfig {

    @ApiModelProperty(value = "业务系统部署的服务域名或主机IP", example = "localhost")
    private String host;

    @ApiModelProperty(value = "上下文路径", example = "/demo")
    private String context;

    @ApiModelProperty(value = "仓库ID", example = "3rd-party")
    private String repoId;

    @ApiModelProperty(value = "令牌名称", example = "zdocs_access_token")
    private String tokenName;

    @ApiModelProperty(value = "默认用户信息")
    private DemoUserConfig admin;

    /**
     * 用户服务的配置信息
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "demo.admin")
    @ApiModel(value = "DemoUserConfig", description = "用户服务的配置信息")
    public static class DemoUserConfig {

        /**
         * 默认用户
         */
        @ApiModelProperty(value = "默认用户", example = "admin")
        private String uname;

        /**
         * 默认密码
         */
        @ApiModelProperty(value = "默认密码", example = "zOffice")
        private String pwd;

        /**
         * 邮箱
         */
        @ApiModelProperty(value = "邮箱", example = "admin@lenovo_zOffice.com")
        private String email;
    }
}
