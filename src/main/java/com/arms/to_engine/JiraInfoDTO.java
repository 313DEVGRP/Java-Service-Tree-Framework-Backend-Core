package com.arms.to_engine;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraInfoDTO {
    private String connectId;
    private String userId;
    private String passwordOrToken;
    private String uri;
}
