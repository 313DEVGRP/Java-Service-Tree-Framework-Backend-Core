package com.arms.api.util.notification.slack;


import java.io.IOException;
import com.slack.api.methods.SlackApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SlackResponse<T> {

    private boolean result;
    private T data;
    private String message;


    public static <T> SlackResponse<T> createSlackResponse(SlackResponseData<T> slackResponseData) {

        String message = "";
        try {
            SlackResponse<T> slackResponse = new SlackResponse<>();
            slackResponse.data = slackResponseData.makeResponseData();
            slackResponse.result = true;
            slackResponse.message = "";
            return slackResponse;
        } catch(SlackApiException | IOException e) {
            // TODO:
            message = e.getMessage();
        }

        SlackResponse<T> slackResponse = new SlackResponse<>();
        slackResponse.data = null;
        slackResponse.result = false;
        slackResponse.message = message;

        return slackResponse;
    }


    public interface SlackResponseData<T> {

        T makeResponseData() throws SlackApiException, IOException;
    }

}
