package com.arms.util.external_communicate.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 히트맵데이터 {
    private Map<String, 히트맵날짜데이터> requirement;
    private Map<String, 히트맵날짜데이터> relationIssue;
    private Map<String, String> requirementColors;
    private Map<String, String> relationIssueColors;
}
