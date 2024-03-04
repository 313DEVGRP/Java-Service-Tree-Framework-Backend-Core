package com.arms.api.util.communicate.external.response.jira;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class 지라서버정보_엔티티 {

    private String connectId;

    private String type;

    private String userId;

    private String passwordOrToken;

    private String uri;

    private String issueId;

    private String issueName;

    private String self;

    private String _class;
}
