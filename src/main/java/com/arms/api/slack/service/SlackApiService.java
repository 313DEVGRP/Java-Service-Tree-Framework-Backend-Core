package com.arms.api.slack.service;

import java.util.List;
import java.util.stream.Collectors;

import com.arms.api.slack.dto.SlackMessageDTO;
import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Attachment;
import com.arms.api.slack.config.SlackProperty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SlackApiService {

    private final SlackProperty slackProperty;


    public SlackResponse<Void> chatPostMessage(final String channel, final SlackMessageDTO slackMessageDTO) {

        List<Attachment> attachments = List.of(slackMessageDTO.parseAttachment());
        Slack slack = Slack.getInstance();
        return SlackResponse.createSlackResponse(() -> {

            ChatPostMessageResponse response = slack.methods(slackProperty.getToken())
                    .chatPostMessage(request -> request.channel(channel).attachments(attachments));
            return null;
        });
    }


    public SlackResponse<Void> chatPostMessage(final SlackProperty.Channel channel,
            final SlackMessageDTO slackMessageDTO) {

        return this.chatPostMessage(channel.name(), slackMessageDTO);
    }
}
