package com.arms.api.salary.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SalaryLogDTO extends TreeBaseDTO {

    private String c_name;

    private String c_key;

    private String c_annual_income;

}
