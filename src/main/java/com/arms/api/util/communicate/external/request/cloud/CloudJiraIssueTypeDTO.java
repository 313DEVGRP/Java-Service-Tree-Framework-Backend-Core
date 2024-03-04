package com.arms.api.util.communicate.external.request.cloud;

import lombok.*;

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
