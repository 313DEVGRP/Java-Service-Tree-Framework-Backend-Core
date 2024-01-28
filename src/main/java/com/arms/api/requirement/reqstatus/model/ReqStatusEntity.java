/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqstatus.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_REQSTATUS")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class ReqStatusEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }
    //@Getter @Setter

    //-- 제품 서비스
    @Column(name = "c_pdservice_link")
    private Long c_pdservice_link;

    @Column(name = "c_pdservice_name")
    @Type(type="text")
    private String c_pdservice_name;

    //-- 제품 서비스 버전
    @Column(name = "c_pds_version_link")
    private Long c_pds_version_link;

    // -- 버전이름에 versionset_link에 대응하는 버전 명 삽입 예정.
    @Column(name = "c_pds_version_name")
    @Type(type="text")
    private String c_pds_version_name;

    @Column(name = "c_req_pdservice_versionset_link")
    @Type(type="text")
    private String c_req_pdservice_versionset_link;

    //-- 제품 서비스 연결 지라 server
    @Column(name = "c_jira_server_link")
    private Long c_jira_server_link;

    @Column(name = "c_jira_server_name")
    @Type(type="text")
    private String c_jira_server_name;

    @Column(name = "c_jira_server_url")
    @Type(type="text")
    private String c_jira_server_url;

    //-- 제품 서비스 연결 지라 프로젝트
    @Column(name = "c_jira_project_link")
    private Long c_jira_project_link;

    @Column(name = "c_jira_project_name")
    private String c_jira_project_name;

    @Column(name = "c_jira_project_key")
    @Type(type="text")
    private String c_jira_project_key;

    @Column(name = "c_jira_project_url")
    @Type(type="text")
    private String c_jira_project_url;

    //-- 요구사항
    @Column(name = "c_req_link")
    private Long c_req_link;

    @Column(name = "c_req_name")
    @Type(type="text")
    private String c_req_name;

    // 요구사항 우선순위
    @Column(name = "c_req_priority_link")
    @Type(type="text")
    private Long c_req_priority_link;

    @Column(name = "c_req_priority_name")
    @Type(type="text")
    private String c_req_priority_name;

    // 요구사항 난이도
    @Column(name = "c_req_difficulty_link")
    @Type(type="text")
    private Long c_req_difficulty_link;

    @Column(name = "c_req_difficulty_name")
    @Type(type="text")
    private String c_req_difficulty_name;


    // 요구사항 상태
    @Column(name = "c_req_state_link")
    @Type(type="text")
    private Long c_req_state_link;

    @Column(name = "c_req_state_name")
    @Type(type="text")
    private String c_req_state_name;

    @Column(name = "c_issue_key")
    @Type(type="text")
    private String c_issue_key;

    @Column(name = "c_issue_url")
    @Type(type="text")
    private String c_issue_url;


    //-- 이슈 우선순위 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )

    @Column(name = "c_issue_priority_link")
    private Long c_issue_priority_link;

    @Column(name = "c_issue_priority_name")
    @Type(type="text")
    private String c_issue_priority_name;

    //-- 이슈 상태 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
    @Column(name = "c_issue_status_link")
    private Long c_issue_status_link;

    @Column(name = "c_issue_status_name")
    @Type(type="text")
    private String c_issue_status_name;

    //-- 이슈 해결책 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
    @Column(name = "c_issue_resolution_link")
    private Long c_issue_resolution_link;

    @Column(name = "c_issue_resolution_name")
    @Type(type="text")
    private String c_issue_resolution_name;

    @Column(name = "c_req_owner")
    @Type(type="text")
    private String c_req_owner;

    @Column(name = "c_issue_reporter")
    @Type(type="text")
    private String c_issue_reporter;

    @Column(name = "c_issue_assignee")
    @Type(type="text")
    private String c_issue_assignee;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_issue_create_date")
    private Date c_issue_create_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_issue_update_date")
    private Date c_issue_update_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_start_date")
    private Date c_req_start_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_end_date")
    private Date c_req_end_date;

    //내용
    @Lob
    @Column(name = "c_contents")
    private String c_contents;

    //설명
    @Column(name = "c_desc")
    @Type(type="text")
    private String c_desc;

    //비고
    @Column(name = "c_etc")
    private String c_etc;



    // '총 작업 MM'
    @Column(name = "c_req_total_resource")
    private Long c_req_total_resource;

    // '총 계획 MM'
    @Column(name = "c_req_plan_resource")
    private Long c_req_plan_resource;

    // '총 기간 Day'
    @Column(name = "c_req_total_time")
    private Long c_req_total_time;

    // '총 계획 Day'
    @Column(name = "c_req_plan_time")
    private Long c_req_plan_time;

    /*
     * Extend Bean Field
     */
	@JsonIgnore
    private Boolean copyBooleanValue;

    @Transient
	@ApiModelProperty(hidden = true)
    public Boolean getCopyBooleanValue() {
        copyBooleanValue = false;
        if (this.getCopy() == 0) {
            copyBooleanValue = false;
        } else {
            copyBooleanValue = true;
        }
        return copyBooleanValue;
    }

    public void setCopyBooleanValue(Boolean copyBooleanValue) {
        this.copyBooleanValue = copyBooleanValue;
    }

    @Override
    public <T extends TreeSearchEntity> void setFieldFromNewInstance(T paramInstance) {
        if( paramInstance instanceof TreeBaseEntity){
            if(paramInstance.isCopied()) {
                this.setC_title("copy_" + this.getC_title());
            }
        }
    }
}
