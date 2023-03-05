package com.egovframework.ple.treeframework.springdata.onlineV2.account.model;

import com.egovframework.ple.treeframework.springhibernate.vo.CoreDTO;
import com.egovframework.ple.treeframework.springhibernate.vo.CoreSearchDTO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "T_USER_SELECTED_ITEM")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SequenceGenerator(name = "S_USER_SELECTED_ITEM", sequenceName = "S_USER_SELECTED_ITEM", allocationSize = 1)
public class CoreSampleEntity extends CoreSearchDTO implements Serializable {

    public CoreSampleEntity() {
        super();
    }

    public CoreSampleEntity(Boolean copyBooleanValue) {
        super();
        this.copyBooleanValue = copyBooleanValue;
    }

    @Override
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="S_USER_SELECTED_ITEM")
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    //@Getter @Setter

    @Column(name="user_cid")
    private String user_cid;

    @Column(name="menu_cid")
    private Long menu_cid;

    @Column(name="compare_item_cid")
    private Long compare_item_cid;

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
