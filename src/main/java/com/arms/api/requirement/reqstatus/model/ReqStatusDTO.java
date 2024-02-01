package com.arms.api.requirement.reqstatus.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReqStatusDTO extends TreeBaseDTO {

    //-- 제품 서비스
    private Long c_pdservice_link;

    private String c_pdservice_name;

    //-- 제품 서비스 버전
    private Long c_pds_version_link;

    private String c_pds_version_name;

    private String c_req_pdservice_versionset_link;

    //-- 제품 서비스 연결 지라 server
    private Long c_jira_server_link;

    private String c_jira_server_name;

    private String c_jira_server_url;

    //-- 제품 서비스 연결 지라 프로젝트
    private Long c_jira_project_link;

    private String c_jira_project_name;

    private String c_jira_project_key;

    private String c_jira_project_url;

    //-- 요구사항
    private Long c_req_link;

    private String c_req_name;

    //-- 요구사항 우선순위
    private Long c_req_priority_link;
    private String c_req_priority_name;

    //-- 요구사항 난이도
    private Long c_req_difficulty_link;
    private String c_req_difficulty_name;

    //-- 요구사항 상태
    private Long c_req_state_link;
    private String c_req_state_name;
    
    private String c_issue_key;

    private String c_issue_url;

    //-- 이슈 우선순위 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
    private Long c_issue_priority_link;

    private String c_issue_priority_name;

    //-- 이슈 상태 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
    private Long c_issue_status_link;

    private String c_issue_status_name;

    //-- 이슈 해결책 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
    private Long c_issue_resolution_link;

    private String c_issue_resolution_name;

    private String c_req_owner;

    private String c_issue_reporter;

    private String c_issue_assignee;

    private Date c_issue_create_date;

    private Date c_issue_update_date;

    private Date c_issue_delete_date; // 이슈 집계 제외 날짜

    private Date c_req_start_date;

    private Date c_req_end_date;

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //비고
    private String c_etc;

    // '총 작업 MM'
    private Long c_req_total_resource;

    // '총 계획 MM'
    private Long c_req_plan_resource;

    // '총 기간 Day'
    private Long c_req_total_time;

    // '총 계획 Day'
    private Long c_req_plan_time;

}
