package com.arms.api.util.notification.slack;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("slack")
public class SlackProperty {

    private String serviceName;
    private String token;
    private String profile;
    private String url;


    public enum Channel {
        backend;

    }

}