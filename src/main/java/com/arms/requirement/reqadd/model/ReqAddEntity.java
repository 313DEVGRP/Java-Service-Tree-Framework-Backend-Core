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
package com.arms.requirement.reqadd.model;

import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.requirement.reqstate.model.ReqStateEntity;
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
public class ReqAddEntity extends TreeSearchEntity implements Serializable {

 	@Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter

    // -- 1:1 Row 단방향 연계 - PdService
    private PdServiceEntity pdServiceEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_req_pdservice_link", referencedColumnName = "c_id")
    public PdServiceEntity getPdServiceEntity() {
        return pdServiceEntity;
    }

    public void setPdServiceEntity(PdServiceEntity pdServiceEntity) {
        this.pdServiceEntity = pdServiceEntity;
    }

    // -- 1:N table 연계는 Java Service Tree Framework 에서 파티셔닝 엔티티를 대상으로는 지원하지 않으므로. 개별 처리 대상
    private Set<PdServiceVersionEntity> pdServiceVersionEntities;

    @Transient
    public Set<PdServiceVersionEntity> getPdServiceVersionEntities() {
        return pdServiceVersionEntities;
    }

    public void setPdServiceVersionEntities(Set<PdServiceVersionEntity> pdServiceVersionEntities) {
        this.pdServiceVersionEntities = pdServiceVersionEntities;
    }

    @Column(name = "c_req_pdservice_versionset_link")
    @Type(type="text")
    private String c_req_pdservice_versionset_link;

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

    @Column(name = "c_req_create_date")
    @Type(type="text")
    private String c_req_create_date;

//    @Column(name = "c_req_priority_link")
//    private Long c_req_priority_link;
//
//    @Column(name = "c_req_state_link")
//    private Long c_req_state_link;

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

    // -- 1:1 table 연계
    // 동적 테이블 이기때문에 글로벌 트리맵에 joinColumns 에 추가하기엔
    // 중복 req c_id 때문에 처리가 불가능하다.
    // 프로그래밍 적인 코드 릴레이션을 처리한다.
    // 대신에 onetoone 처리도 잘 되는걸 확인했다.
    // 글로벌 트리맵에서 관리하도록 하자.

    // -- 1:1 Row 단방향 연계
    private ReqPriorityEntity reqPriorityEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_req_priority_link", referencedColumnName = "c_id")
    public ReqPriorityEntity getReqPriorityEntity() {
        return reqPriorityEntity;
    }

    public void setReqPriorityEntity(ReqPriorityEntity reqPriorityEntity) {
        this.reqPriorityEntity = reqPriorityEntity;
    }


    // -- 1:1 Row 단방향 연계
    private ReqStateEntity reqStateEntity;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "c_req_state_link", referencedColumnName = "c_id")
    public ReqStateEntity getReqStateEntity() {
        return reqStateEntity;
    }

    public void setReqStateEntity(ReqStateEntity reqStateEntity) {
        this.reqStateEntity = reqStateEntity;
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
