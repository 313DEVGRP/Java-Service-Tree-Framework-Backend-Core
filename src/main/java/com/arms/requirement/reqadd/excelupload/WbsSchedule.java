package com.arms.requirement.reqadd.excelupload;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WbsSchedule {

	@EqualsAndHashCode.Include
	private String wbsName;
	private String jobName;
	private String parentWbsName;
	private int depth;

	public String getParentWbsName(){

		if(!wbsName.isEmpty()&& isBranch()){
			return wbsName.substring(0,wbsName.lastIndexOf("."));
		}
		return "-";
	}


	public boolean isBranch(){
		return depth>1;
	}
}
