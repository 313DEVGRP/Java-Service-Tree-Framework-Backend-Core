package com.arms.api.util.external_communicate.dto.cloud;


import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CloudJiraIssueInputDTO {
    private FieldsDTO fields;
}
