package com.arms.notification.slack;


import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SlackService {

    private final SlackApiService slackApiService;


    public void sendMessageToChannel(final SlackProperty.Channel channel, final Exception e) {
        slackApiService.chatPostMessage(channel, e);
    }


}
