package com.arms.api.jira.jiraserver.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 계정정보_데이터 {
    String self;
    String name;
    String emailAddress;
    String displayName;
    Boolean active;
    Boolean admin;
    String api_key;
}
