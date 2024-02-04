package com.arms.egovframework.javaservice.treeframework.excel;

import java.io.InputStream;
import java.io.OutputStream;

public class ExcelUtilsFactory {


    public static ExcelUtilsBase getInstance(InputStream inputStream) {
        return new ExcelUtilsXlsx(inputStream);
    }

    public static ExcelUtilsBase getInstance(OutputStream outputStream) {
        return new ExcelUtilsXlsx(outputStream);
    }

}
