package com.arms.api.util.communicate.external.response.jira;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class 지라이슈필드_데이터 {


    private 프로젝트 project;

    private 지라이슈유형_데이터 issuetype;

    private String summary;

    private String description;

    private 보고자 reporter;

    private 담당자 assignee;

    private List<String> labels;

    private List<연결된_이슈> issuelinks;

    private List<지라이슈_데이터> subtasks;

    private 지라이슈우선순위_데이터 priority;

    private 지라이슈상태_데이터 status;

    private 지라이슈해결책_데이터 resolution;

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class 프로젝트 {
        private String self;
        private String id;
        private String key;
        private String name;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class 보고자 {
        private String name;
        private String emailAddress;
        private String displayName;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class 담당자 {
        private String name;
        private String emailAddress;
        private String displayName;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class 연결된_이슈 {
        private String self;
        private String id;
        private 유형 type;
        private 지라이슈_데이터 inwardIssue;
        private 지라이슈_데이터 outwardIssue;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 유형 {
        private String self;
        private String id;
        private String name;
        private String inward;
        private String outward;
    }


}
