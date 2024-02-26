package com.arms.api.analysis.cost.dto;

import com.arms.egovframework.javaservice.treeframework.excel.ExcelClassAnnotation;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelFieldAnnotation;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.annotations.Cache;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@Table(name = "T_ARMS_ANNUAL_INCOME")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@ExcelClassAnnotation(sheetName = "Sheet2", headerRowSize = 1, headerTitleName = "연봉 정보")
@AllArgsConstructor
@NoArgsConstructor
public class 연봉엔티티 extends TreeSearchEntity implements Serializable {

    @Override
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "c_id")
    public Long getC_id() {
        return super.getC_id();
    }

    @ExcelFieldAnnotation(columnIndex = 0, formatting = "%.0f", headerName = "이름")
    @Column(name = "c_name")
    private String c_name;

    @ExcelFieldAnnotation(columnIndex = 1, formatting = "%.0f", headerName = "고유 키")
    @Column(name = "c_key")
    private String c_key;

    @ExcelFieldAnnotation(columnIndex = 2, formatting = "%.0f", headerName = "연봉(만원)")
    @Column(name = "c_annual_income")
    private String c_annual_income;

    @JsonIgnore
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
    @ApiIgnore
    public <T extends TreeSearchEntity> void setFieldFromNewInstance(T paramInstance) {
        if( paramInstance instanceof TreeBaseEntity){
            if(paramInstance.isCopied()) {
                this.setC_title("copy_" + this.getC_title());
            }
        }
    }
}