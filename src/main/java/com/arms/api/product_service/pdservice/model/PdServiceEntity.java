/*
 * @author Dongmin.lee
 * @since 2022-06-17
 * @version 22.06.17
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.product_service.pdservice.model;

import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
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
@Table(name = "T_ARMS_PDSERVICE")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class PdServiceEntity extends TreeSearchEntity implements Serializable {

    @Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter
    @Column(name="c_pdservice_owner")
    @Type(type="text")
    private String c_pdservice_owner;

    @Column(name="c_pdservice_reviewer01")
    @Type(type="text")
    private String c_pdservice_reviewer01;

    @Column(name="c_pdservice_reviewer02")
    @Type(type="text")
    private String c_pdservice_reviewer02;

    @Column(name="c_pdservice_reviewer03")
    @Type(type="text")
    private String c_pdservice_reviewer03;

    @Column(name="c_pdservice_reviewer04")
    @Type(type="text")
    private String c_pdservice_reviewer04;

    @Column(name="c_pdservice_reviewer05")
    private String c_pdservice_reviewer05;

    @Column(name="c_pdservice_writer")
    @Type(type="text")
    private String c_pdservice_writer;

    //내용
    @Lob
    @Column(name="c_pdservice_contents")
    private String c_pdservice_contents;

    //설명
    @Type(type="text")
    @Column(name = "c_pdservice_desc")
    private String c_pdservice_desc;

    //비고
    @Column(name = "c_pdservice_etc")
    private String c_pdservice_etc;

    // -- 1:N table 연계
    private Set<PdServiceVersionEntity> pdServiceVersionEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "GLOBAL_TREE_MAP",
            joinColumns = @JoinColumn(name = "pdservice_link"),
            inverseJoinColumns = @JoinColumn(name = "pdserviceversion_link")
    )
    @WhereJoinTable( clause =   "filerepository_link IS NULL and " +
                                "jiraserver_link IS NULL and " +
                                "jiraproject_link IS NULL and " +
                                "jiraissuepriority_link IS NULL and " +
                                "jiraissueresolution_link IS NULL and " +
                                "jiraissuestatus_link IS NULL and " +
                                "jiraissuetype_link IS NULL")
    public Set<PdServiceVersionEntity> getPdServiceVersionEntities() {
        return pdServiceVersionEntities;
    }

    public void setPdServiceVersionEntities(Set<PdServiceVersionEntity> pdServiceVersionEntities) {
        this.pdServiceVersionEntities = pdServiceVersionEntities;
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
