package com.arms.util.external_communicate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 지라_이슈_필드_데이터_전송_객체 {


    private 프로젝트 project;

    private 지라_이슈_유형_데이터_전송_객체 issuetype;

    private String summary;

    private String description;

    private 보고자 reporter;

    private 담당자 assignee;

    private List<String> labels;

    private List<연결된_이슈> issuelinks;

    private List<지라_이슈_데이터_전송_객체> subtasks;

    private 지라_이슈_우선순위_데이터_전송_객체 priority;

    private 지라_이슈_상태_데이터_전송_객체 status;

    private 지라_이슈_해결책_데이터_전송_객체 resolution;

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
    public static class 보고자 {
        private String name;
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
        private String name;
        private String emailAddress;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class 연결된_이슈 {
        private String self;
        private String id;
        private 유형 type;
        private 지라_이슈_데이터_전송_객체 inwardIssue;
        private 지라_이슈_데이터_전송_객체 outwardIssue;
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
