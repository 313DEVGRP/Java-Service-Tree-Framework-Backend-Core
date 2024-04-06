package com.arms.api.analysis.salary.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDTO extends TreeBaseDTO {

    @JsonProperty("이름")
    private String c_name;

    @JsonProperty("키")
    private String c_key;

    @JsonProperty("연봉")
    private String c_annual_income;

    @JsonIgnore
    private Long c_id;

    @JsonIgnore
    private long ref;

    @JsonIgnore
    private Long c_position;

    @JsonIgnore
    private String c_title;

    @JsonIgnore
    private String c_type;

    @JsonIgnore
    private long copy;

    @JsonIgnore
    private long multiCounter;

}