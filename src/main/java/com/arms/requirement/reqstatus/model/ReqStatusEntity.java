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
package com.arms.requirement.reqstatus.model;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(name = "c_pds_version_name")
    @Type(type="text")
    private String c_pds_version_name;

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

    //-- 요구사항 우선 순위 ( 이슈 우선순위 적용 : 지라 서버에 등록된 JIRA Issue priority
    private JiraIssuePriorityEntity reqIssuePriorityEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_req_priority_link", referencedColumnName = "c_id")
    public JiraIssuePriorityEntity getReqIssuePriorityEntity() {
        return reqIssuePriorityEntity;
    }

    public void setReqIssuePriorityEntity(JiraIssuePriorityEntity reqIssuePriorityEntity) {
        this.reqIssuePriorityEntity = reqIssuePriorityEntity;
    }

    @Column(name = "c_req_priority_name")
    @Type(type="text")
    private String c_req_priority_name;

    //-- 요구사항 상태 ( 지라 서버에 등록된 JIRA Issue status
    private JiraIssueStatusEntity reqIssueStatusEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_req_status_link", referencedColumnName = "c_id")
    public JiraIssueStatusEntity getReqIssueStatusEntity() {
        return reqIssueStatusEntity;
    }

    public void setReqIssueStatusEntity(JiraIssueStatusEntity reqIssueStatusEntity) {
        this.reqIssueStatusEntity = reqIssueStatusEntity;
    }

    @Column(name = "c_req_status_name")
    @Type(type="text")
    private String c_req_status_name;

    //-- 요구사항 이슈
    @Column(name = "c_issue_link")
    private Long c_issue_link;

    @Column(name = "c_issue_key")
    @Type(type="text")
    private String c_issue_key;

    @Column(name = "c_issue_url")
    @Type(type="text")
    private String c_issue_url;


    //-- 요구사항 이슈 우선순위
    private JiraIssuePriorityEntity jiraIssuePriorityEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_issue_priority_link", referencedColumnName = "c_id")
    public JiraIssuePriorityEntity getJiraIssuePriorityEntity() {
        return jiraIssuePriorityEntity;
    }

    public void setJiraIssuePriorityEntity(JiraIssuePriorityEntity jiraIssuePriorityEntity) {
        this.jiraIssuePriorityEntity = jiraIssuePriorityEntity;
    }

    @Column(name = "c_issue_priority_name")
    @Type(type="text")
    private String c_issue_priority_name;

    //-- 요구사항 이슈 상태
    private JiraIssueStatusEntity jiraIssueStatusEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_issue_status_link", referencedColumnName = "c_id")
    public JiraIssueStatusEntity getJiraIssueStatusEntity() {
        return jiraIssueStatusEntity;
    }

    public void setJiraIssueStatusEntity(JiraIssueStatusEntity jiraIssueStatusEntity) {
        this.jiraIssueStatusEntity = jiraIssueStatusEntity;
    }

    @Column(name = "c_issue_status_name")
    @Type(type="text")
    private String c_issue_status_name;

    //-- 요구사항 이슈 해결책
    @Column(name = "c_issue_resolution_link")
    private Long c_issue_resolution_link;

    @Column(name = "c_issue_resolution_name")
    @Type(type="text")
    private String c_issue_resolution_name;

    @Column(name = "c_create_date")
    private Date c_create_date;

    //-- 요구사항 이슈 연관, 서브 이슈 서머리
    @Column(name = "c_parent_issue_id")
    @Type(type="text")
    private Long c_parent_issue_id;

    @Column(name = "c_this_issue_type")
    @Type(type="text")
    private String c_this_issue_type;

    //-- 기타
    @Column(name = "c_req_status_etc")
    @Type(type="text")
    private String c_req_status_etc;

    @Lob
    @Column(name = "c_req_status_contents")
    private String c_req_status_contents;

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
