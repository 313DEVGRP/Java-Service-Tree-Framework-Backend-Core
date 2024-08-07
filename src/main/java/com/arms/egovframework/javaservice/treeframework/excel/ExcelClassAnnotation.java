package com.arms.egovframework.javaservice.treeframework.excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelClassAnnotation {

    String sheetName();
    int headerTitleRowSize() default 1;
    String headerTitleName() default "";
    int headerRowSize();
}