package com.arms.dashboard.model.combination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequirementJiraIssueAggregationResponse {
    private long totalIssues;
    private Map<String, Long> statuses;
    private long totalRequirements;
}