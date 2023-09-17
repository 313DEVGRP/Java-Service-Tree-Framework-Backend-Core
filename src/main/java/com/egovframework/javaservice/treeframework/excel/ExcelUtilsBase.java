package com.egovframework.javaservice.treeframework.excel;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

@Slf4j
public abstract class ExcelUtilsBase {

    protected InputStream inputStream;
    protected OutputStream outputStream;

    public ExcelUtilsBase(OutputStream outputStream)  {
            this.outputStream = outputStream;
    }

    public ExcelUtilsBase(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public <T> void create(List<T> srcContent, Class<T> cls) throws IOException {
        Workbook workbook = null;
        try {
            String sheetName = getExportSheetName(cls);
            List<CellTemplate> templateList = getExportableFields(cls);

            workbook = createOrGetWorkBook(true);
            fillWorkBook(workbook, sheetName, templateList, srcContent);

            workbook.write(this.outputStream);
        } catch (Exception ex) {
            log.info("ExcelUtilsBase :: create :: Exception -> " + ex.getMessage());
        } finally {
            close(workbook, this.outputStream);
        }
    }

    public <T> List<T> read(Class<T> cls) throws Exception {
        Workbook workbook = null;
        try {
            workbook = createOrGetWorkBook(false);
            Sheet sheet = readSheet(cls, workbook);
            return readContent(sheet, cls);
        }  finally {
            close(workbook);
        }
    }


    public  List<List<?>> read(List<Class<?>> list) throws Exception {
        Workbook workbook = null;
        try {
            workbook = createOrGetWorkBook(false);
            return readSheets(list, workbook);

        }  finally {
            close(workbook);
        }
    }

    protected abstract Workbook createBlankNewWorkbook();

    protected abstract Workbook getWorkbook() throws IOException;

    protected abstract <T> void fillWorkBook(Workbook workbook, String sheetName, List<CellTemplate> templateList, List<T> srcContent) throws IllegalArgumentException, IllegalAccessException;

    private <T> Workbook createOrGetWorkBook(boolean createNewIfNotFound) throws  IOException {

        if(this.inputStream!=null){
            return getWorkbook();
        }

        if(this.outputStream!=null){
            return createBlankNewWorkbook();
        }

        return createBlankNewWorkbook();

    }

    private Sheet getSheetBySheetName(Workbook workbook, String sheetName) {
        Iterator<Sheet> iterator = workbook.sheetIterator();
        while (iterator.hasNext()) {
            Sheet sheet = iterator.next();
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        throw new RuntimeException("Can't find sheet " + sheetName);
    }

    private List<Sheet> getSheetBySheetNames(Workbook workbook, List<String> sheetNames) {
        Iterator<Sheet> iterator = workbook.sheetIterator();
        List<Sheet> sheetList = new ArrayList<>();
        while (iterator.hasNext()) {
            Sheet sheet = iterator.next();
            sheetNames.stream().filter(sheetName->sheet.getSheetName().equals(sheetName))
                .findFirst()
                .ifPresent(sheetName->sheetList.add(sheet));
        }

        return sheetList;

    }


    private <T> Sheet readSheet(Class<T> cls, Workbook workbook) {
        String sheetName = getExportSheetName(cls);

        return getSheetBySheetName(workbook, sheetName);
    }

    private List<List<?>>readSheets(List<Class<?>> classList, Workbook workbook) {

        List<List<?>> sheets = new ArrayList<>();

        classList.forEach(cls->{
            Annotation[] annotations = cls.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ExcelClassAnnotation) {
                    Sheet sheetBySheetName
                        = getSheetBySheetName(workbook,((ExcelClassAnnotation) annotation).sheetName());
                    try {
                        System.out.println(sheetBySheetName.getSheetName());
                        sheets.add(readContent(sheetBySheetName, cls));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return sheets;
    }

    private <T> List<T> readContent(Sheet sheet, Class<T> cls) throws Exception {
        List<T> result = new ArrayList<>();
        List<CellTemplate> templateList = getExportableFields(cls);
        Iterator<Row> rows = sheet.rowIterator();
        IntStream.range(0,getHeaderSize(cls)).forEach(i-> skipExcelHeader(rows));
        while (rows.hasNext()) {
            Row row = rows.next();
            T item = readRow(cls, templateList, row);
            result.add(item);
        }

        return result;
    }

    private void skipExcelHeader(Iterator<Row> rows) {
        rows.next();
    }

    private <T> T readRow(Class<T> cls, List<CellTemplate> templateList, Row row) throws Exception{
        T item = cls.getConstructor().newInstance();
        int cellLength = row.getLastCellNum();
        int columnIndex = 0;

        for(int i =0;i<cellLength;i++){
            Cell cell = row.getCell(i);
            templateList.get(columnIndex).invokeObjectProperty(cell, item);
            columnIndex++;
        }
        return item;
    }

    private <T> String getExportSheetName(Class<T> cls) {
        Annotation[] annotations = cls.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ExcelClassAnnotation) {
                return ((ExcelClassAnnotation) annotation).sheetName();
            }
        }
        throw new IllegalArgumentException(cls + "is not exportable.");
    }

    private List<String> getExportSheetNames(List<Class<?>> classList) {

        List<String> exportSheetNames = new ArrayList<>();

        classList.forEach(cls->{
            Annotation[] annotations = cls.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof ExcelClassAnnotation) {
                    exportSheetNames.add(((ExcelClassAnnotation) annotation).sheetName());
                }
            }
        });

        return exportSheetNames;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class SheetInfos{
        private Sheet sheet;
        private Class<?> aClass;
    }

    private <T> int getHeaderSize(Class<T> cls){
        Annotation[] annotations = cls.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof ExcelClassAnnotation) {
                return ((ExcelClassAnnotation) annotation).headerSize();
            }
        }
        throw new IllegalArgumentException(cls + "is not exportable.");
    }


    private <T> List<CellTemplate> getExportableFields(Class<T> cls) {
        List<CellTemplate> templateList = new ArrayList<>();
        Field[] declaredFields = cls.getDeclaredFields();

        for (Field field : declaredFields) {
            CellTemplate template = CellTemplate.getInstance(field);
            if (template != null) {
                templateList.add(template);
            }
        }
        Collections.sort(templateList);
        return templateList;
    }

    protected void close(Closeable... itemsToClose) throws IOException {
        if (itemsToClose != null) {
            for (Closeable closeable : itemsToClose) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        }
    }
}
