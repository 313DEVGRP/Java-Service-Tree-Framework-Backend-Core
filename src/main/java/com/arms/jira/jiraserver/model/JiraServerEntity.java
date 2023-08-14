/*
 * @author Dongmin.lee
 * @since 2023-03-28
 * @version 23.03.28
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.jira.jiraserver.model;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_JIRASERVER")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class JiraServerEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter
    @Column(name = "c_jira_server_name")
    @Type(type="text")
    private String c_jira_server_name;

    @Column(name = "c_jira_server_base_url")
    @Type(type="text")
    private String c_jira_server_base_url;

    @Column(name = "c_jira_server_type")
    @Type(type="text")
    private String c_jira_server_type;

    @Column(name = "c_jira_server_connect_id")
    @Type(type="text")
    private String c_jira_server_connect_id;

    @Column(name = "c_jira_server_connect_pw")
    @Type(type="text")
    private String c_jira_server_connect_pw;

    @Lob
    @Column(name = "c_jira_server_contents")
    private String c_jira_server_contents;

    @Column(name = "c_jira_server_etc")
    @Type(type="text")
    private String c_jira_server_etc;

    // -- 1:N table 연계 - 단방향
    private Set<JiraIssuePriorityEntity> jiraIssuePriorityEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TREEMAP_JIRASERVER_PROJECT",
            joinColumns = @JoinColumn(name = "jiraserver_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuepriority_link")
    )
    public Set<JiraIssuePriorityEntity> getJiraIssuePriorityEntities() {
        return jiraIssuePriorityEntities;
    }

    public void setJiraIssuePriorityEntities(Set<JiraIssuePriorityEntity> jiraIssuePriorityEntities) {
        this.jiraIssuePriorityEntities = jiraIssuePriorityEntities;
    }

    // -- 1:N table 연계 - 단방향
    private Set<JiraIssueResolutionEntity> jiraIssueResolutionEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TREEMAP_JIRASERVER_PROJECT",
            joinColumns = @JoinColumn(name = "jiraserver_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissueresolution_link")
    )
    public Set<JiraIssueResolutionEntity> getJiraIssueResolutionEntities() {
        return jiraIssueResolutionEntities;
    }

    public void setJiraIssueResolutionEntities(Set<JiraIssueResolutionEntity> jiraIssueResolutionEntities) {
        this.jiraIssueResolutionEntities = jiraIssueResolutionEntities;
    }

    // -- 1:N table 연계
    private Set<JiraIssueStatusEntity> jiraIssueStatusEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TREEMAP_JIRASERVER_PROJECT",
            joinColumns = @JoinColumn(name = "jiraserver_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuestatus_link")
    )
    public Set<JiraIssueStatusEntity> getJiraIssueStatusEntities() {
        return jiraIssueStatusEntities;
    }

    public void setJiraIssueStatusEntities(Set<JiraIssueStatusEntity> jiraIssueStatusEntities) {
        this.jiraIssueStatusEntities = jiraIssueStatusEntities;
    }

    // -- 1:N table 연계
    private Set<JiraIssueTypeEntity> jiraIssueTypeEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TREEMAP_JIRASERVER_PROJECT",
            joinColumns = @JoinColumn(name = "jiraserver_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuetype_link")
    )
    public Set<JiraIssueTypeEntity> getJiraIssueTypeEntities() {
        return jiraIssueTypeEntities;
    }

    public void setJiraIssueTypeEntities(Set<JiraIssueTypeEntity> jiraIssueTypeEntities) {
        this.jiraIssueTypeEntities = jiraIssueTypeEntities;
    }

    // -- 1:N table 연계 - 단방향
    private Set<JiraProjectEntity> jiraProjectEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "TREEMAP_JIRASERVER_PROJECT",
            joinColumns = @JoinColumn(name = "jiraserver_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraproject_link")
    )
    public Set<JiraProjectEntity> getJiraProjectEntities() {
        return jiraProjectEntities;
    }

    public void setJiraProjectEntities(Set<JiraProjectEntity> jiraProjectEntities) {
        this.jiraProjectEntities = jiraProjectEntities;
    }


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
