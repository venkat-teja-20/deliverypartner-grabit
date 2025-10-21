package com.grabit.config;

import com.grabit.mapper.OrderURLMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public OrderURLMapper orderURLMapper(){
        return new OrderURLMapper(System.getenv("order_url"));
    }
}
