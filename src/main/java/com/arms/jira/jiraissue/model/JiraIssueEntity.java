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
package com.arms.jira.jiraissue.model;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
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

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_JIRAISSUE")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter

    @Column(name = "c_issue_id")
    @Type(type="text")
    private String c_issue_id;

    @Column(name = "c_issue_url")
    @Type(type="text")
    private String c_issue_url;

    @Column(name = "c_issue_desc")
    @Type(type="text")
    private String c_issue_desc;

    @Column(name = "c_issue_key")
    @Type(type="text")
    private String c_issue_key;

    @Column(name = "c_issue_summary")
    @Type(type="text")
    private String c_issue_summary;

    @Column(name = "c_issue_type")
    @Type(type="text")
    private String c_issue_type;

    @Column(name = "c_issue_labels")
    @Type(type="text")
    private String c_issue_labels;

    @Column(name = "c_issue_components")
    @Type(type="text")
    private String c_issue_components;

    @Column(name = "c_issue_link_yn")
    @Type(type="text")
    private String c_issue_link_yn;

    @Column(name = "c_issue_subtask_yn")
    @Type(type="text")
    private String c_issue_subtask_yn;

    @Column(name = "c_issue_affected_versions")
    @Type(type="text")
    private String c_issue_affected_versions;

    @Column(name = "c_issue_fix_versions")
    @Type(type="text")
    private String c_issue_fix_versions;

    @Column(name = "c_issue_create_date")
    @Type(type="text")
    private String c_issue_create_date;

    @Column(name = "c_issue_update_date")
    @Type(type="text")
    private String c_issue_update_date;

    @Column(name = "c_issue_due_date")
    @Type(type="text")
    private String c_issue_due_date;

    @Column(name = "c_issue_status")
    private Long c_issue_status;

    @Column(name = "c_issue_priority")
    private Long c_issue_priority;

    @Column(name = "c_issue_resolution")
    private Long c_issue_resolution;

    //---

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraissue_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuepriority_link")
    )
    @WhereJoinTable( clause = "jiraissuepriority_link is not null")
    private JiraIssuePriorityEntity jiraIssuePriorityEntity;


    //---

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraissue_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissueresolution_link")
    )
    @WhereJoinTable( clause = "jiraissueresolution_link is not null")
    private JiraIssueResolutionEntity jiraIssueResolutionEntity;


    //---

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraissue_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuestatus_link")
    )
    @WhereJoinTable( clause = "jiraissuestatus_link is not null")
    private JiraIssueStatusEntity jiraIssueStatusEntity;


    //---

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraissue_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuetype_link")
    )
    @WhereJoinTable( clause = "jiraissuetype_link is not null")
    private JiraIssueTypeEntity jiraIssueTypeEntity;


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
