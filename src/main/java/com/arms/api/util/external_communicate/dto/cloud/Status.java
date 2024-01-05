package com.arms.api.util.external_communicate.dto.cloud;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private String id;
    private String name;
    private String description;
    private String self;
}
