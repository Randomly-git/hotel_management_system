package com.hotel.hotel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("精品单体酒店智能管理系统 API")
                        .description("基于AI的酒店管理系统，包含口碑量化、个性化服务和动态定价三大模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("项目组")
                                .email("project@hotel.com")
                                .url("https://github.com/hotel-management"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}