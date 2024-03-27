package com.arms.api.util.communicate.external.response.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class 지라이슈_데이터 {


    // 공통 필드
    private String id;

    private String key;

    private String self;

    // 특정 이슈 조회 시 사용
    private 지라이슈필드_데이터 fields;

    // 특정 프로젝트의 전체 이슈 조회 시 사용
    private List<지라이슈_데이터> issues;

}
