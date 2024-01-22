package com.arms.api.slack.dto;

import com.slack.api.model.Attachment;
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


    public Attachment parseAttachment() {

        return Attachment.builder().title(this.title).titleLink(this.titleLink).authorName(this.authorName)
                .text(this.text).footer(footer).build();
    }

}
