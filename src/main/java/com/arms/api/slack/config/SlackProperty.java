package com.arms.api.slack.config;

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
        무소식이희소식;

    }

}
