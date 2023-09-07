package com.arms.util.external_communicate;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraInfoEntity {

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
