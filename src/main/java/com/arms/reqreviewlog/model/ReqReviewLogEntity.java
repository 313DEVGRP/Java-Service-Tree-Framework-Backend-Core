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
package com.arms.reqreviewlog.model;

import com.egovframework.ple.treeframework.model.TreeBaseEntity;
import com.egovframework.ple.treeframework.model.TreeLogBaseEntity;
import com.egovframework.ple.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "T_ARMS_REQREVIEWLOG")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReqReviewLogEntity extends TreeLogBaseEntity implements Serializable {

    public ReqReviewLogEntity() {
        super();
    }

    public ReqReviewLogEntity(Boolean copyBooleanValue) {
        super();
        this.copyBooleanValue = copyBooleanValue;
    }

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter
    @Column(name = "c_pdservice_link")
    private Long c_pdservice_link;

    @Column(name = "c_version_link")
    private Long c_version_link;

    @Column(name = "c_req_link")
    private Long c_req_link;

    @Column(name = "c_review_sender")
    @Type(type="text")
    private String c_review_sender;

    @Column(name = "c_review_responder")
    @Type(type="text")
    private String c_review_responder;

    @Column(name = "c_review_creat_date")
    @Type(type="text")
    private String c_review_creat_date;

    @Column(name = "c_review_result_state")
    @Type(type="text")
    private String c_review_result_state;

    @Column(name = "c_review_result_date")
    @Type(type="text")
    private String c_review_result_date;
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