package com.egovframework.ple.treeframework.springhibernate.vo;

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
@Table(name = "T_COMPREHENSIVETREE_HIBER")
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SequenceGenerator(name = "S_COMPREHENSIVETREE_HIBER", sequenceName = "S_COMPREHENSIVETREE_HIBER", allocationSize = 1)
public class CoreDTO extends CoreSearchDTO implements Serializable {

	public CoreDTO() {
		super();
	}

	public CoreDTO(Boolean copyBooleanValue) {
		super();
		this.copyBooleanValue = copyBooleanValue;
	}

	@Override
	@Id
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="S_COMPREHENSIVETREE_HIBER")
	@Column(name = "c_id")
	public Long getC_id() {
		return super.getC_id();
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
