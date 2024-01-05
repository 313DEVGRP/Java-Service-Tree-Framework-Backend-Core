/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.egovframework.javaservice.treeframework.validation.custom.validator;

import com.arms.egovframework.javaservice.treeframework.validation.custom.constraints.Contained;
import org.apache.commons.lang.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ContainedValidator implements ConstraintValidator<Contained, String> {
	
	/*
	 * @Contained를 통해 지정한 스트링 값들
	 */
	private String[] values;
	@Override
	public void initialize(Contained constraintAnnotation) {
		this.values = constraintAnnotation.values();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(values.length == 0) {
			return true;
		}
		
		for(String s: values){

            if (StringUtils.isEmpty(value) || s.equals(value)) {
                return true;
            }

		}
		return false;
	}
}
