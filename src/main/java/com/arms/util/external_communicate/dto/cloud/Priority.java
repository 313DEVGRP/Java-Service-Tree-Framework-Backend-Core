package com.arms.util.external_communicate.dto.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Priority {
        private String self;
        private String id;
        private String name;
        private String description;
        private boolean isDefault;
}
