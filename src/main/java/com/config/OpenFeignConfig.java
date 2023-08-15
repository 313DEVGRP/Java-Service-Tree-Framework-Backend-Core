package com.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.arms.util.external_communicate")
public class OpenFeignConfig {
}
