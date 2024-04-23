package com.arms.api.requirement.reqstatuslog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class ReqStatusLogDTO extends TreeBaseDTO {


    private Long c_pdservice_link;

    private String c_pdservice_name;

    //-- 제품 서비스 버전
    private Long c_version_link;

    private String c_pds_version_name;

    //-- 제품 서비스 연결 지라 프로젝트
    private Long c_jira_link;

    private String c_jira_key;

    private String c_jira_url;

    //-- 제품 서비스 연결 지라 프로젝트 버전
    private Long c_jira_version_link;

    private String c_jira_version_name;

    private String c_jira_version_url;

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

    private String c_issue_summery;

    private String c_issue_url;


    //-- 요구사항 이슈 우선순위
    private Long c_issue_priority_link;

    private String c_issue_priority_name;

    private String c_issue_priority_url;


    //-- 요구사항 이슈 상태
    private Long c_issue_status_link;

    private String c_issue_status_name;

    private String c_issue_status_url;

    //-- 요구사항 이슈 해결책
    private Long c_issue_resolution_link;

    private String c_issue_resolution_name;

    private String c_issue_resolution_url;

    //-- 요구사항 리뷰 상황
    private String c_req_review_status;

    //-- 요구사항 생성자
    private String c_writer;

    private Date c_create_date;

    //-- 요구사항 이슈 연관, 서브 이슈 서머리
    private String c_issue_link_issue_summary;

    private String c_issue_sub_issue_summary;

    //-- 기타
    private String c_req_status_etc;

    private String c_req_status_contents;

}
