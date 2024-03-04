package com.arms.api.util.communicate.external.request.cloud;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Priority {
        private String self;
        private String id;
        private String name;
        private String description;
        private boolean isDefault;
}
