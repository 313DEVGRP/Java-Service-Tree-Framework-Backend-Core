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
package com.arms.api.requirement.reqadd_pure.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
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
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_REQADD")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class ReqAddPureEntity extends TreeSearchEntity implements Serializable {

    @Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    @Column(name = "c_req_pdservice_link")
    @Type(type="text")
    private String c_req_pdservice_link;

    @Column(name = "c_req_pdservice_versionset_link")
    @Type(type="text")
    private String c_req_pdservice_versionset_link;

    @Column(name = "c_req_priority_link")
    @Type(type="text")
    private Long c_req_priority_link;

    @Column(name = "c_req_state_link")
    @Type(type="text")
    private Long c_req_state_link;

    @Column(name = "c_req_difficulty_link")
    @Type(type="text")
    private Long c_req_difficulty_link;


    @Column(name = "c_req_reviewer01")
    @Type(type="text")
    private String c_req_reviewer01;

    @Column(name = "c_req_reviewer02")
    private String c_req_reviewer02;

    @Column(name = "c_req_reviewer03")
    @Type(type="text")
    private String c_req_reviewer03;

    @Column(name = "c_req_reviewer04")
    @Type(type="text")
    private String c_req_reviewer04;

    @Column(name = "c_req_reviewer05")
    @Type(type="text")
    private String c_req_reviewer05;

    @Column(name = "c_req_reviewer01_status")
    @Type(type="text")
    private String c_req_reviewer01_status;

    @Column(name = "c_req_reviewer02_status")
    @Type(type="text")
    private String c_req_reviewer02_status;

    @Column(name = "c_req_reviewer03_status")
    @Type(type="text")
    private String c_req_reviewer03_status;

    @Column(name = "c_req_reviewer04_status")
    @Type(type="text")
    private String c_req_reviewer04_status;

    @Column(name = "c_req_reviewer05_status")
    @Type(type="text")
    private String c_req_reviewer05_status;

    @Column(name = "c_req_writer")
    @Type(type="text")
    private String c_req_writer;

    @Column(name = "c_req_owner")
    @Type(type="text")
    private String c_req_owner;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_create_date")
    private Date c_req_create_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_update_date")
    private Date c_req_update_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_start_date")
    private Date c_req_start_date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_req_end_date")
    private Date c_req_end_date;

    @Column(name = "c_req_total_resource")
    private Long c_req_total_resource; //'총 작업 MM',

    @Column(name = "c_req_plan_resource")
    private Long c_req_plan_resource; // '총 계획 MM',

    @Column(name = "c_req_total_time")
    private Long c_req_total_time; // '총 기간 Day',

    @Column(name = "c_req_plan_time")
    private Long c_req_plan_time; // '총 계획 Day',

    @Column(name = "c_req_plan_progress")
    private Long c_req_plan_progress; // '계획 진척도',

    @Column(name = "c_req_performance_progress")
    private Long c_req_performance_progress; // '실적 진척도',

    @Column(name = "c_req_manager")
    private String c_req_manager; // '담당자'

    @Column(name = "c_req_output")
    private String c_req_output; // '산출물'

    //내용
    @Lob
    @Column(name = "c_req_contents")
    private String c_req_contents;

    //설명
    @Column(name = "c_req_desc")
    @Type(type="text")
    private String c_req_desc;

    //비고
    @Column(name = "c_req_etc")
    private String c_req_etc;


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
