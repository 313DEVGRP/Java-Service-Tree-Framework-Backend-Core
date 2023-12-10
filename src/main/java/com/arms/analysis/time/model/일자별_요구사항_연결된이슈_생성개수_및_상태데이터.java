package com.arms.analysis.time.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 일자별_요구사항_연결된이슈_생성개수_및_상태데이터 {
    private long totalRequirements;
    private Map<String, Long> requirementStatuses;
    private long totalRelationIssues;
    private Map<String, Long> relationIssueStatuses;
}