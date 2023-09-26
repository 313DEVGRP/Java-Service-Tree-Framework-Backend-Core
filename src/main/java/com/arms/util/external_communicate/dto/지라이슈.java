package com.arms.util.external_communicate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 지라이슈 {

    private String id;
    private Long jira_server_id;
    private Date timestamp;
    private String issueID;
    private String key;
    private String self;
    private String parentReqKey;
    private Boolean isReq;

    private Object etc;

    private List<String> percolatorQueries;
    private 지라이슈.프로젝트 project;
    private 지라이슈.이슈유형 issuetype;
    private 지라이슈.생성자 creator;
    private 지라이슈.보고자 reporter;
    private 지라이슈.담당자 assignee;
    private List<String> labels;
    private 지라이슈.우선순위 priority;
    private 지라이슈.상태 status;
    private 지라이슈.해결책 resolution;
    private String resolutiondate;
    private String created;

    private String updated;

    private List<지라이슈.워크로그> worklogs;

    private Integer timespent;

    private String summary;

    private Long pdServiceId;
    private Long pdServiceVersion;

    private String _class; //추가

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class 프로젝트 {

        @JsonProperty("project_self")
        private String self;

        @JsonProperty("project_id")
        private String id;

        @JsonProperty("project_key")
        private String key;

        @JsonProperty("project_name")
        private String name;
    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 이슈유형 {
        // 온프레미스, 클라우드 공통
        @JsonProperty("issuetype_self")
        private String self;
        @JsonProperty("issuetype_id")
        private String id;
        @JsonProperty("issuetype_description")
        private String description;
        @JsonProperty("issuetype_name")
        private String name;
        @JsonProperty("issuetype_subtask")
        private Boolean subtask;

        // 클라우드만 사용
        @JsonProperty("issuetype_untranslatedName")
        private String untranslatedName;
        @JsonProperty("issuetype_hierarchyLevel")
        private Integer hierarchyLevel;
    }


    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 생성자 {

        @JsonProperty("creator_accountId")
        private String accountId;
        @JsonProperty("creator_emailAddress")
        private String emailAddress;

    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 보고자 {

        @JsonProperty("reporter_accountId")
        private String accountId;
        @JsonProperty("reporter_emailAddress")
        private String emailAddress;

    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 담당자 {

        @JsonProperty("assignee_accountId")
        private String accountId;
        @JsonProperty("assignee_emailAddress")
        private String emailAddress;

    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 우선순위 {

        // 온프레미스, 클라우드 공통
        @JsonProperty("priority_self")
        private String self;

        @JsonProperty("priority_id")
        private String id;

        @JsonProperty("priority_name")
        private String name;

        @JsonProperty("priority_description")
        private String description;

        // 클라우드
        @JsonProperty("priority_isDefault")
        private boolean isDefault;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 상태 {

        // 온프레미스, 클라우드 공통
        @JsonProperty("status_self")
        private String self;

        @JsonProperty("status_id")
        private String id;

        @JsonProperty("status_name")
        private String name;

        @JsonProperty("status_description")
        private String description;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 해결책 {


        // 온프레미스, 클라우드 공통
        @JsonProperty("resolution_self")
        private String self;

        @JsonProperty("resolution_id")
        private String id;

        @JsonProperty("resolution_name")
        private String name;

        @JsonProperty("resolution_description")
        private String description;

        // 클라우드
        @JsonProperty("resolution_isDefault")
        private boolean isDefault;
    }


    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 워크로그 {

        @JsonProperty("worklogs_self")
        private String self;

        private 저자 author;

        private 수정한_저자 updateAuthor;

        @JsonProperty("worklogs_created")
        private String created;

        @JsonProperty("worklogs_updated")
        private String updated;

        @JsonProperty("worklogs_started")
        private String started;

        @JsonProperty("worklogs_timeSpent")
        private String timeSpent;

        @JsonProperty("worklogs_timeSpentSeconds")
        private Integer timeSpentSeconds;

        @JsonProperty("worklogs_id")
        private String id;

        @JsonProperty("worklogs_issueId")
        private String issueId;
    }


    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 저자 {

        @JsonProperty("worklogs_author_accountId")
        private String accountId;

        @JsonProperty("worklogs_author_emailAddress")
        private String emailAddress;

    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 수정한_저자 {

        @JsonProperty("worklogs_updateAuthor_accountId")
        private String accountId;
        @JsonProperty("worklogs_updateAuthor_emailAddress")
        private String emailAddress;
    }
}
