package com.arms.util.external_communicate.dto.cloud;

import lombok.*;

import java.net.URI;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudJiraIssueTypeDTO {

    private URI self;
    private Long id;
    private String name;
    private boolean isSubtask;
    private String description;
    private URI iconUri;

}
