package com.egovframework.ple.treeframework.springhibernate.vo;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public class CoreLogDTO extends CoreSearchDTO implements Serializable {

    //@Getter @Setter
    @Column(name="C_DATAID")
    private Long C_DATAID;

    @Column(name="C_METHOD")
    private String c_method;

    @Column(name="C_STATE")
    private String c_state;

    @Column(name="C_DATE")
    private Date c_date;

    public Long getC_DATAID() {
        return C_DATAID;
    }

    public void setC_DATAID(Long c_DATAID) {
        C_DATAID = c_DATAID;
    }

    public String getC_method() {
        return c_method;
    }

    public void setC_method(String c_method) {
        this.c_method = c_method;
    }

    public String getC_state() {
        return c_state;
    }

    public void setC_state(String c_state) {
        this.c_state = c_state;
    }

    public Date getC_date() {
        return c_date;
    }

    public void setC_date(Date c_date) {
        this.c_date = c_date;
    }

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
