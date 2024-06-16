package com.arms.api.analysis.cost.model;

import lombok.*;

import java.util.Map;

@Setter
@Getter
public class 버전요구사항별_담당자데이터 {
    private Map<String, Map<String, Map<String, 담당자데이터>>> 버전_요구사항_담당자;
    private Map<String, 담당자데이터> 전체담당자목록;

    @Getter
    @Builder
    public static class 담당자데이터 {
        private String 이름;
        private Long 연봉;
    }
}
