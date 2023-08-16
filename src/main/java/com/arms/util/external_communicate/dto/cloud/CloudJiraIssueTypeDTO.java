package com.arms.util.external_communicate.dto.cloud;

import lombok.*;

import java.net.URI;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudJiraIssueTypeDTO {

    private String self;
    private String id;
    private String description;
    private String name;
    private String untranslatedName;
    private Boolean subtask;
    private Integer hierarchyLevel;

}
