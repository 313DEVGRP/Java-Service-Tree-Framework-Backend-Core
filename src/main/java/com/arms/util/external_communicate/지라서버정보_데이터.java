package com.arms.util.external_communicate;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 지라서버정보_데이터 {
    private String connectId;

    private String type;

    private String userId;

    private String passwordOrToken;

    private String uri;
}