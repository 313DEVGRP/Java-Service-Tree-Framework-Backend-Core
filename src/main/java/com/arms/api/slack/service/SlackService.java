package com.arms.api.slack.service;

import java.util.List;

import com.arms.api.slack.config.SlackProperty;
import com.arms.api.slack.dto.SlackMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SlackService {

    private final SlackApiService slackApiService;


    public void sendMessageToChannel(final SlackProperty.Channel channel, final SlackMessageDTO slackMessageDTO) {
        slackApiService.chatPostMessage(channel, slackMessageDTO);
    }


}
