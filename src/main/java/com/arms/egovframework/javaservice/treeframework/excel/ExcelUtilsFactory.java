package com.arms.egovframework.javaservice.treeframework.excel;

import java.io.InputStream;

public class ExcelUtilsFactory {


    public static ExcelUtilsBase getInstance(InputStream inputStream) {
        return new ExcelUtilsXlsx(inputStream);
    }

}
