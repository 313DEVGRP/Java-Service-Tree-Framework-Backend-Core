package com.arms.util.external_communicate.dto.onpremise;

import lombok.*;

import java.net.URI;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnPremiseJiraPriorityDTO {

    private String self;
    private String id;
    private String name;
    private String description;
}