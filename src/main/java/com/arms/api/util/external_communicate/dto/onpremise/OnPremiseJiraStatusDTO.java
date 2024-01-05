package com.arms.api.util.external_communicate.dto.onpremise;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraStatusDTO {

    private String self;

    private String id;

    private String name;

    private String description;
}
