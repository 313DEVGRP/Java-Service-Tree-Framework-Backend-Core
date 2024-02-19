package com.arms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost","http://127.0.0.1","http://localhost:9999","http://127.0.0.1:9999","http://313.co.kr","http://www.313.co.kr","http://313.co.kr:9999","http://www.313.co.kr:9999","http://a-rms.net","http://www.a-rms.net","http://www.a-rms.net:9999","http://a-rms.net:9999","http://mad-dev.megazone.com")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.HEAD.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name());
    }

}
