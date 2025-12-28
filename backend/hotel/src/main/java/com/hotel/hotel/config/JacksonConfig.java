package com.hotel.hotel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson配置类
 * 提供ObjectMapper Bean等JSON序列化配置
 */
@Configuration
public class JacksonConfig {

    /**
     * 提供ObjectMapper Bean
     * @return 配置好的ObjectMapper实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册JavaTimeModule以支持Java 8时间类型
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}