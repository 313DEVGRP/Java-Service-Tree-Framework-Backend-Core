package com.arms.api.util.notification.slack;


import com.slack.api.model.Attachment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SlackMessageDTO {

    private String title;
    private String titleLink;
    private String authorName;
    private String text;
    private String footer;

    @Builder
    private SlackMessageDTO(String title, String titleLink, String authorName, String text, String footer) {
        this.title = title;
        this.titleLink = titleLink;
        this.authorName = authorName;
        this.text = text;
        this.footer = footer;
    }

    public Attachment parseAttachment() {

        return Attachment.builder().title(this.title).titleLink(this.titleLink).authorName(this.authorName)
                .text(this.text).footer(footer).build();
    }

}
