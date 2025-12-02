package com.filez.demo.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Third-party business system configuration information
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "demo")
@ApiModel(value = "DemoConfig", description = "Business system configuration information")
public class DemoConfig {

    @ApiModelProperty(value = "Service schema", example = "http")
    private String schema;

    @ApiModelProperty(value = "Service domain or host IP where the business system is deployed", example = "localhost")
    private String host;

    @ApiModelProperty(value = "Context path", example = "/demo")
    private String context;

    @ApiModelProperty(value = "Repository ID", example = "3rd-party")
    private String repoId;

    @ApiModelProperty(value = "Token name", example = "zdocs_access_token")
    private String tokenName;

    @ApiModelProperty(value = "Default user information")
    private DemoUserConfig admin;

    /**
     * User service configuration information
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "demo.admin")
    @ApiModel(value = "DemoUserConfig", description = "User service configuration information")
    public static class DemoUserConfig {

        /**
         * Default user
         */
        @ApiModelProperty(value = "Default user", example = "admin")
        private String uname;

        /**
         * Default password
         */
        @ApiModelProperty(value = "Default password", example = "zOffice")
        private String pwd;

        /**
         * Email
         */
        @ApiModelProperty(value = "Email", example = "admin@lenovo_zOffice.com")
        private String email;
    }
}
