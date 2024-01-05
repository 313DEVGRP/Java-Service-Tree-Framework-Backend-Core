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
package com.arms.egovframework.javaservice.treeframework.validation.custom.constraints;

import com.arms.egovframework.javaservice.treeframework.validation.custom.validator.ContainedValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContainedValidator.class)
public @interface Contained {
	
	String message() default "The target field's mapping value is not contained in the given string array.";
	
	String[] values() default {};
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
