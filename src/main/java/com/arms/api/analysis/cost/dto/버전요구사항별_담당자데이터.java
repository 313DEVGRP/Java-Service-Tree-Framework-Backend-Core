package com.arms.api.analysis.cost.dto;

import lombok.*;

import java.util.Map;

@Setter
@Getter
public class 버전요구사항별_담당자데이터 {
    private Map<String, 버전데이터> 버전;
    private Map<String, 담당자데이터> 전체담당자목록;

    @Getter
    @Builder
    public static class 버전데이터 {
        private Map<String,요구사항데이터> 요구사항;
    }

    @Getter
    @Builder
    public static class 요구사항데이터 {
        private Map<String,담당자데이터> 담당자;
    }

    @Getter
    @Builder
    public static class 담당자데이터 {
        private String 이름;
        private Long 연봉;
        private Long 성과;
    }
}
