package com.arms.notification.slack;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SlackNotificationService {

    private final SlackProperty slackProperty;
    private final Environment environment;


    public void sendMessageToChannel(final SlackProperty.Channel channel, final Exception e) {
        if (isLive()) {
            String title = MessageFormat.format("[{0}] {1}", slackProperty.getProfile(), slackProperty.getServiceName());
            String text = messageInStackTrace(e);

            SlackMessageDTO slackMessageDTO = SlackMessageDTO.builder()
                    .title(title)
                    .text(text)
                    .build();
            List<Attachment> attachments = List.of(slackMessageDTO.parseAttachment());

            Slack slack = Slack.getInstance();
            try {
                slack.methods(slackProperty.getToken()).chatPostMessage(request ->
                        request.channel(channel.name()).attachments(attachments));
            } catch (Exception exception) {
                log.error("Failed to send Slack message: {}", exception.getMessage(), exception);
            }
        }
    }

    private String messageInStackTrace(Exception e) {
        String filteredStackTrace = Arrays.stream(e.getStackTrace())
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("com.arms"))
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n", "\n\n[StackTrace]\n", ""));

        return e + (filteredStackTrace.isBlank() ? "" : filteredStackTrace);
    }

    private boolean isLive() {
        return Arrays.asList(environment.getActiveProfiles()).contains("live");
    }
}