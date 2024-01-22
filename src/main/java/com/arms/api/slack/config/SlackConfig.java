package com.arms.api.slack.config;

import com.arms.api.slack.service.SlackApiService;
import com.arms.api.slack.service.SlackService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = SlackProperty.class)
public class SlackConfig {
    @Bean
    public SlackProperty slackProperty() {
        return new SlackProperty();
    }

    @Bean
    public SlackApiService slackService() {
        return new SlackApiService(slackProperty());
    }

    @Bean
    public SlackService slackClient() {
        return new SlackService(slackService());
    }
}
