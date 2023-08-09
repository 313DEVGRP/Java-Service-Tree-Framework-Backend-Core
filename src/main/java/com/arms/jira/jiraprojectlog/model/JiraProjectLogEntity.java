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
package com.arms.jira.jiraprojectlog.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.egovframework.javaservice.treeframework.model.TreeLogBaseEntity;
import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_JIRAPROJECT_LOG")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectLogEntity extends TreeLogBaseEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter

    @Lob
    @Column(name = "c_jira_contents")
    private String c_jira_contents;

    @Column(name = "c_jira_etc")
    @Type(type="text")
    private String c_jira_etc;

    @Column(name = "c_jira_url")
    @Type(type="text")
    private String c_jira_url;

    @Column(name = "c_jira_id")
    @Type(type="text")
    private String c_jira_id;

    @Column(name = "c_jira_key")
    @Type(type="text")
    private String c_jira_key;

    @Column(name = "c_jira_name")
    @Type(type="text")
    private String c_jira_name;


    @Column(name = "c_jira_avatar_48")
    @Type(type="text")
    private String c_jira_avatar_48;

    @Column(name = "c_jira_avatar_32")
    @Type(type="text")
    private String c_jira_avatar_32;

    @Column(name = "c_jira_avatar_24")
    @Type(type="text")
    private String c_jira_avatar_24;

    @Column(name = "c_jira_avatar_16")
    @Type(type="text")
    private String c_jira_avatar_16;

    @Column(name = "c_jira_category_url")
    @Type(type="text")
    private String c_jira_category_url;

    @Column(name = "c_jira_category_id")
    @Type(type="text")
    private String c_jira_category_id;

    @Column(name = "c_jira_category_name")
    @Type(type="text")
    private String c_jira_category_name;

    @Column(name = "c_jira_category_desc")
    @Type(type="text")
    private String c_jira_category_desc;

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
