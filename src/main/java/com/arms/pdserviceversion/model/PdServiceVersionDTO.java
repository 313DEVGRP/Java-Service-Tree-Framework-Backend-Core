/*
 * @author Dongmin.lee
 * @since 2022-11-20
 * @version 22.11.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.pdserviceversion.model;

import com.egovframework.ple.treeframework.springhibernate.vo.CoreDTO;
import com.egovframework.ple.treeframework.springhibernate.vo.CoreSearchDTO;
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
@Table(name = "T_ARMS_PDSERVICEVERSION")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SequenceGenerator(name = "S_T_ARMS_PDSERVICEVERSION", sequenceName = "S_T_ARMS_PDSERVICEVERSION", allocationSize = 1)
public class PdServiceVersionDTO extends CoreSearchDTO implements Serializable {

    public PdServiceVersionDTO() {
        super();
    }

    public PdServiceVersionDTO(Boolean copyBooleanValue) {
        super();
        this.copyBooleanValue = copyBooleanValue;
    }

    @Override
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="S_T_ARMS_PDSERVICEVERSION")
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter
    @Column(name = "c_start_date")
    private String c_start_date;

    @Column(name = "c_end_date")
    private String c_end_date;

    @Column(name = "c_pdservice_link")
    private String c_pdservice_link;

    @Lob
    @Column(name="C_CONTENTS")
    private String c_contents;

    /*
     * Extend Bean Field
     */
    private Boolean copyBooleanValue;

    @Transient
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
    public <T extends CoreSearchDTO> void setFieldFromNewInstance(T paramInstance) {
        if( paramInstance instanceof CoreDTO){
            if(paramInstance.isCopied()) {
                this.setC_title("copy_" + this.getC_title());
            }
        }
    }
}
