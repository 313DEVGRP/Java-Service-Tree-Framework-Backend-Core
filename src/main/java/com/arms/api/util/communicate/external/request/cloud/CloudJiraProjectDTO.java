package com.arms.api.util.communicate.external.request.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudJiraProjectDTO {
    private String self;

    private String id;

    private String key;

    private String name;
}