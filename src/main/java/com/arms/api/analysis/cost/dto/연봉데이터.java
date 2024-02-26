package com.arms.api.analysis.cost.dto;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class 연봉데이터 extends TreeBaseDTO {

    @JsonProperty("이름")
    private String c_name;

    @JsonProperty("키")
    private String c_key;

    @JsonProperty("연봉")
    private String c_annual_income;

}