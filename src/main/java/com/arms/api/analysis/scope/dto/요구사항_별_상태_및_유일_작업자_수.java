package com.arms.api.analysis.scope.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class 요구사항_별_상태_및_유일_작업자_수 {

    private String key;

    private String summary;

    private String status;

    private Integer uniqueAssignees;
}
