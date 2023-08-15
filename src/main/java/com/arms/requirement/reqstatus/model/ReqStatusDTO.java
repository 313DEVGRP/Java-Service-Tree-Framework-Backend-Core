package com.arms.requirement.reqstatus.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
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

    //-- 제품 서비스 연결 지라 server
    private Long c_jira_server_link;

    private String c_jira_server_name;

    private String c_jira_server_url;

    //-- 제품 서비스 연결 지라 프로젝트
    private Long c_jira_project_link;

    private String c_jira_project_key;

    private String c_jira_project_url;

    //-- 요구사항
    private Long c_req_link;

    private String c_req_name;

    //-- 요구사항 우선 순위
    private Long c_req_priority_link;

    private String c_req_priority_name;

    //-- 요구사항 상태
    private Long c_req_status_link;

    private String c_req_status_name;

    //-- 요구사항 이슈
    private Long c_issue_link;

    private String c_issue_key;

    private String c_issue_url;

    //-- 요구사항 이슈 우선순위
    private Long c_issue_priority_link;

    private String c_issue_priority_name;

    //-- 요구사항 이슈 상태
    private Long c_issue_status_link;

    private String c_issue_status_name;

    //-- 요구사항 이슈 해결책
    private Long c_issue_resolution_link;

    private String c_issue_resolution_name;

    private Date c_create_date;

    //-- 요구사항 이슈 연관, 서브 이슈 서머리
    private Long c_parent_issue_id;

    private String c_this_issue_type;

    //-- 기타
    private String c_req_status_etc;

    private String c_req_status_contents;

}
