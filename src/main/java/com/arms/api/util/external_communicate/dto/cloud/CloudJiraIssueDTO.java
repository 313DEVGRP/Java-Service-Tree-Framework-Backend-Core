package com.arms.api.util.external_communicate.dto.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudJiraIssueDTO {
    private String id;
    private String key;
    private String self;

    private FieldsDTO fields;
    private List<CloudJiraIssueDTO> issues;

    public CloudJiraIssueDTO(String id, String key, String self) {
        this.key = key;
        this.id = id;
        this.self = self;
        this.issues = new ArrayList<>();
    }
}
