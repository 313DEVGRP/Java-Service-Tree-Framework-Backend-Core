package com.arms.api.util.communicate.external.request.onpremise;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraResolutionDTO {

    private String self;   // AddressableNamedEntity

    private String id;

    private String name; // AddressableNamedEntity

    private String description;
}
