package com.arms.notification.slack;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = SlackProperty.class)
public class SlackConfig {
    @Bean
    public SlackApiService slackService(SlackProperty slackProperty) {
        return new SlackApiService(slackProperty);
    }

    @Bean
    public SlackService slackClient(SlackApiService slackApiService) {
        return new SlackService(slackApiService);
    }
}
