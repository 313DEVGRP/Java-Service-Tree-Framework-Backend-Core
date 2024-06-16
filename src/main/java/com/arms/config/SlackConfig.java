package com.arms.config;

import com.arms.api.util.slack.SlackNotificationService;
import com.arms.api.util.slack.SlackProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties(value = SlackProperty.class)
public class SlackConfig {

    @Bean
    public SlackNotificationService slackNotificationService(SlackProperty slackProperty, Environment environment) {
        return new SlackNotificationService(slackProperty, environment);
    }
}
