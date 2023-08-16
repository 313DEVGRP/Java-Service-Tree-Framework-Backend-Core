package com.arms.util.external_communicate.dto.onpremise;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraIssueTypeDto {
    private String self;

    private String id;

    private String description;

    private String name;

    private Boolean subtask;
}
