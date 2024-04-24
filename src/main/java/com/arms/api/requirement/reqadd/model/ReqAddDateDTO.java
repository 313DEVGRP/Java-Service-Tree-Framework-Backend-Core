package com.arms.api.requirement.reqadd.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class ReqAddDateDTO extends TreeBaseDTO {
    private Date c_req_start_date;
    private Date c_req_end_date;
    private Long c_req_total_resource; //'총 작업 MM',
    private Long c_req_plan_resource; // '총 계획 MM',
    private Long c_req_total_time; // '총 기간 Day',
    private Long c_req_plan_time; // '총 계획 Day',
}
