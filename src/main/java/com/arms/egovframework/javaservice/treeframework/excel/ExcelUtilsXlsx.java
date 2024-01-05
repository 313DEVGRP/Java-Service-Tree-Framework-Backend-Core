package com.arms.egovframework.javaservice.treeframework.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtilsXlsx extends ExcelUtilsBase {

    public ExcelUtilsXlsx(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    protected Workbook createBlankNewWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    protected Workbook getWorkbook() throws  IOException {
        try (InputStream inputStream = this.inputStream) {
            return new XSSFWorkbook(inputStream);
        }
    }

    @Override
    public <T> void fillWorkBook(Workbook workbook, String sheetName, List<CellTemplate> templateList, List<T> srcContent) throws IllegalArgumentException, IllegalAccessException {
        XSSFSheet sheet = ((XSSFWorkbook) workbook).createSheet(sheetName);
        createHeaderRow(templateList, sheet);
        createBodyRows(templateList, srcContent, sheet);
    }

    private void createHeaderRow(List<CellTemplate> templateList, XSSFSheet sheet) {
        XSSFRow headerRow = sheet.createRow(0);
        for (CellTemplate template : templateList) {
            template.createHeaderCell(headerRow);
        }
    }

    private <T> void createBodyRows(List<CellTemplate> templateList, List<T> srcContent, XSSFSheet sheet) throws IllegalAccessException {
        int rowNum = 1;
        for (T item : srcContent) {
            XSSFRow row = sheet.createRow(rowNum++);
            for (CellTemplate template : templateList) {
                template.createCell(row, item);
            }
        }
    }

}
