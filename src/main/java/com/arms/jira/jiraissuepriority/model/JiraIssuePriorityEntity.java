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
package com.arms.jira.jiraissuepriority.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseEntity;
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
@Table(name = "T_ARMS_JIRAISSUEPRIORITY")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssuePriorityEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }
    
    //@Getter @Setter

    //온프라미스 대응 : private final Long id;
    //클라우드 : private String id;
    @Column(name = "c_issue_priority_id")
    @Type(type="text")
    private String c_issue_priority_id;

    //온프레미스 대응: private final String description;
    //클라우드 : private String description;
    @Column(name = "c_issue_priority_desc")
    @Type(type="text")
    private String c_issue_priority_desc;

    //온프라미스 대응 : private final String name;
    //클라우드 : private String name;
    @Column(name = "c_issue_priority_name")
    @Type(type="text")
    private String c_issue_priority_name;

    //온프라미스 대응 : private final URI self;
    //클라우드 : private String self;
    @Column(name = "c_issue_priority_url")
    @Type(type="text")
    private String c_issue_priority_url;

    //내용
    @Lob
    @Column(name = "c_contents")
    private String c_contents;

    //설명
    @Column(name = "c_desc")
    @Type(type="text")
    private String c_desc;

    //클라우드 대응 : private boolean isDefault;
    //비고
    @Column(name = "c_etc")
    private String c_etc;

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
