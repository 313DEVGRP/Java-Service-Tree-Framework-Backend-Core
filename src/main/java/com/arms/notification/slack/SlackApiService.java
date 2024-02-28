package com.arms.notification.slack;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.slack.api.Slack;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Attachment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SlackApiService {

    private final SlackProperty slackProperty;


    private String messageInStackTrace(Exception e) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add(e.toString());
        stringJoiner.add("\n[StackTrace]");
        Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("com.arms"))
                .forEach(stackTraceElement -> stringJoiner.add(stackTraceElement.toString()));

        return stringJoiner.toString();
    }

    public SlackResponse<Void> chatPostMessage(final SlackProperty.Channel channel,
                                               final Exception e) {

        String title = MessageFormat.format("[{0}] {1}", slackProperty.getProfile(), slackProperty.getServiceName());
        SlackMessageDTO slackMessageDTO = SlackMessageDTO.builder()
                .title(title)
                .text(messageInStackTrace(e))
                .build();
        List<Attachment> attachments = List.of(slackMessageDTO.parseAttachment());
        Slack slack = Slack.getInstance();
        return SlackResponse.createSlackResponse(() -> {

            ChatPostMessageResponse response = slack.methods(slackProperty.getToken())
                    .chatPostMessage(request -> request.channel(channel.name()).attachments(attachments));
            return null;
        });
    }
}
