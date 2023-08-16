package com.arms.util.external_communicate.dto.onpremise;

import lombok.*;

import java.net.URI;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraPriorityDTO {

    private URI self;    // AddressableNamedEntity

    private Long id;    // extends BasicPriority

    private String name; // AddressableNamedEntity

    private String statusColor;

    private String description;

    private URI iconUri;
}