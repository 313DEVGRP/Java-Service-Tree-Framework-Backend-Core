package com.arms.api.util.external_communicate.dto.cloud;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public  class Resolution {
    private String self;
    private String id;
    private String name;
    private String description;
    private boolean isDefault;
}
