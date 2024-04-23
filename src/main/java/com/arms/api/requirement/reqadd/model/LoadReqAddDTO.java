package com.arms.api.requirement.reqadd.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper=true)
@Getter
@Setter
public class LoadReqAddDTO extends TreeBaseDTO {
    private Long c_req_pdservice_link;
    private String c_req_pdservice_versionset_link;
    private String c_req_writer;
    private String c_req_owner;
    private Date c_req_create_date;
    private Date c_req_update_date;
    private Date c_req_start_date;
    private Date c_req_end_date;
    private Long c_req_total_resource;
    private Long c_req_plan_resource;
    private Long c_req_total_time;
    private Long c_req_plan_time;
    private Long c_req_plan_progress;
    private Long c_req_performance_progress;
    private String c_req_manager;
    private String c_req_output;
    private Long c_req_priority_link;
    private Long c_req_state_link;
    private Long c_req_difficulty_link;
    private String c_req_etc;
    private String c_req_desc;
    private String c_req_contents;
//    @Mapping(source = "pdServiceEntity.c_id", target = "c_req_pdservice_link")
//    @Mapping(source = "reqPriorityEntity.c_id", target = "c_req_priority_link")
//    @Mapping(source = "reqStateEntity.c_id", target = "c_req_state_link")
//    @Mapping(source = "reqDifficultyEntity.c_id", target = "c_req_difficulty_link")
}
