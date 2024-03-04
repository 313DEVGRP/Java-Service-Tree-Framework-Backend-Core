package com.arms.api.util.communicate.external.request.onpremise;

import lombok.*;

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