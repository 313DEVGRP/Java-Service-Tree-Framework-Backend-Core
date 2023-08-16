package com.arms.util.external_communicate.dto.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
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
