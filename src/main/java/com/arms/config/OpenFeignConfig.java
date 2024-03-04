package com.arms.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.arms.api.util.communicate")
public class OpenFeignConfig {
}
