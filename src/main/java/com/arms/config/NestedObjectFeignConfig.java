package com.arms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import feign.Feign;
import feign.hystrix.HystrixFeign;

@Configuration
public class NestedObjectFeignConfig {

	@Bean
	@Scope("prototype")
	@ConditionalOnProperty(name = "feign.hystrix.enabled")
	public Feign.Builder feignHystrixBuilder() {
		HystrixFeign.Builder encoder = HystrixFeign.builder();
		encoder.queryMapEncoder(new CustomNestedObjectQueryMapEncoder());

		return encoder;
	}

}
