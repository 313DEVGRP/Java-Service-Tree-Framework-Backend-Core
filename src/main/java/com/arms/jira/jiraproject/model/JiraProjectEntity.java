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
package com.arms.jira.jiraproject.model;

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
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_JIRAPROJECT")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter
    //클라우드 대응 : private String key;
    //온프라미스 대응 : private String key;
    @Column(name = "c_jira_key")
    @Type(type="text")
    private String c_jira_key;

    //클라우드 대응 : private String name;
    //온프라미스 대응 : private String name;
    @Column(name = "c_jira_name")
    @Type(type="text")
    private String c_jira_name;

    //클라우드 대응 : private String self;
    //온프라미스 대응 : private String self;
    @Column(name = "c_jira_url")
    @Type(type="text")
    private String c_jira_url;

    //내용
    @Lob
    @Column(name = "c_contents")
    private String c_contents;

    //클라우드 대응 : private String id;
    //온프라미스 대응 : private String id;
    //설명
    @Column(name = "c_desc")
    @Type(type="text")
    private String c_desc;

    //비고
    @Column(name = "c_etc")
    private String c_etc;


    // 1:N table 연계
    private Set<JiraIssueTypeEntity> jiraIssueTypeEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraproject_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuetype_link")
    )
    @WhereJoinTable( clause =   "filerepository_link IS NULL and " +
                                "pdservice_link IS NULL and " +
                                "pdserviceversion_link IS NULL and " +
                                "jiraserver_link IS NULL and " +
                                "jiraissuepriority_link IS NULL and " +
                                "jiraissueresolution_link IS NULL and " +
                                "jiraissuestatus_link IS NULL")
    public Set<JiraIssueTypeEntity> getJiraIssueTypeEntities() { return jiraIssueTypeEntities; }

    public void setJiraIssueTypeEntities(Set<JiraIssueTypeEntity> jiraIssueTypeEntities) {
        this.jiraIssueTypeEntities = jiraIssueTypeEntities;
    }

    private Set<JiraIssueStatusEntity> jiraIssueStatusEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "jiraproject_link"),
            inverseJoinColumns = @JoinColumn(name = "jiraissuestatus_link")
    )
    @WhereJoinTable( clause =   "filerepository_link IS NULL and " +
                                "pdservice_link IS NULL and " +
                                "pdserviceversion_link IS NULL and " +
                                "jiraserver_link IS NULL and " +
                                "jiraissuepriority_link IS NULL and " +
                                "jiraissueresolution_link IS NULL and " +
                                "jiraissuetype_link IS NULL")
    public Set<JiraIssueStatusEntity> getJiraIssueStatusEntities() { return jiraIssueStatusEntities; }

    public void setJiraIssueStatusEntities(Set<JiraIssueStatusEntity> jiraIssueStatusEntities) {
        this.jiraIssueStatusEntities = jiraIssueStatusEntities;
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
