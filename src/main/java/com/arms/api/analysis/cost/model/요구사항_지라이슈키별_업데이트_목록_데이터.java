package com.arms.api.analysis.cost.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 요구사항_지라이슈키별_업데이트_목록_데이터 {
    String key;
    String parentReqKey;
    String updated;
    String resolutiondate;
    Boolean isReq;
}
