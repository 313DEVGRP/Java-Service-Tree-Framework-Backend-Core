package com.arms.requirement.reqadd.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqAddDTO extends TreeBaseDTO {

    private Long c_req_pdservice_link;
    
    private String c_req_pdservice_versionset_link;

    private String c_req_reviewer01;

    private String c_req_reviewer02;

    private String c_req_reviewer03;

    private String c_req_reviewer04;

    private String c_req_reviewer05;

    private String c_req_reviewer01_status;

    private String c_req_reviewer02_status;

    private String c_req_reviewer03_status;

    private String c_req_reviewer04_status;

    private String c_req_reviewer05_status;

    private String c_req_writer;

    private String c_req_owner;

    private Date c_req_create_date;

    private Date c_req_update_date;

    private Date c_req_start_date;

    private Date c_req_end_date;

    private Long c_req_total_resource; //'총 작업 MM',

    private Long c_req_plan_resource; // '총 계획 MM',

    private Long c_req_total_time; // '총 기간 Day',

    private Long c_req_plan_time; // '총 계획 Day',

    private Long c_req_plan_progress; // '계획 진척도',

    private Long c_req_performance_progress; // '실적 진척도',

    private String c_req_manager; // '담당자'

    private String c_req_output; // '산출물'

    private String c_req_contents;

    private String c_req_desc;

    private String c_req_etc;
}
