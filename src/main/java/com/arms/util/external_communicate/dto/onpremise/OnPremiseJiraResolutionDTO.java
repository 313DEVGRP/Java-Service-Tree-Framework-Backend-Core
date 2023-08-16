package com.arms.util.external_communicate.dto.onpremise;

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
