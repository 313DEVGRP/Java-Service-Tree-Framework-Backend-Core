package com.arms.egovframework.javaservice.treeframework.excel;

import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

public class CellTemplate implements Comparable<CellTemplate> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm");

    private Field field;
    private String annotation_headerName;
    private int annotation_columnIndex;
    private String annotation_formatting;

    public static CellTemplate getInstance(Field field) {
        CellTemplate mappingCell = new CellTemplate();
        Annotation[] annotations = field.getAnnotations();
        if (annotations == null || annotations.length == 0) {
            return null;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof ExcelFieldAnnotation) {
                ExcelFieldAnnotation res = (ExcelFieldAnnotation) annotation;
                mappingCell.field = field;
                mappingCell.annotation_columnIndex = res.columnIndex();
                mappingCell.annotation_headerName = res.headerName();
                mappingCell.annotation_formatting = res.formatting();
                if (mappingCell.annotation_headerName == null || mappingCell.annotation_headerName.trim().length() == 0) {
                    mappingCell.annotation_headerName = field.getName();
                }
                return mappingCell;
            }
        }

        return null;
    }

    public void createCell(Row row, Object obj)
            throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        Cell cell = row.createCell(annotation_columnIndex);

        boolean isAccessible = field.canAccess(obj);
        field.setAccessible(true);

        Class<?> fieldClass = field.getType();
        Class<?> aClass = Class.forName(field.getType().getName());

        if(field.get(obj)==null){
            cell.setCellValue("");
        }else if (fieldClass == int.class) {
            int value = field.getInt(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Integer.class) {

            cell.setCellValue((Integer)field.get(obj));
        } else if (fieldClass == Short.class || fieldClass == short.class) {
            short value = field.getShort(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Long.class || fieldClass == long.class) {
            long value = field.getShort(obj);
            cell.setCellValue(value);
        } else if (fieldClass == String.class) {
            String value = Optional.ofNullable(field.get(obj)).map(Object::toString).orElse(null);
            cell.setCellValue(value);
        } else if (fieldClass == Double.class || fieldClass == double.class) {
            double value = field.getDouble(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Float.class || fieldClass == float.class) {
            float value = field.getFloat(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Byte.class || fieldClass == byte.class) {
            byte value = field.getByte(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Character.class || fieldClass == char.class) {
            char value = field.getChar(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Boolean.class) {
            boolean value = field.getBoolean(obj);
            cell.setCellValue(value);
        } else if (fieldClass == Date.class) {
            String value = dateFormat.format((Date) field.get(obj));
            cell.setCellValue(value);
        } else if(aClass.isEnum()){
            cell.setCellValue(String.valueOf(field.get(obj)));
        }else {
            cell.setCellValue("");
        }

        CellStyle cellHeaderStyle = row.getSheet().getWorkbook().createCellStyle();

        cellHeaderStyle.setBorderTop(BorderStyle.THIN);
        cellHeaderStyle.setBorderBottom(BorderStyle.THIN);
        cellHeaderStyle.setBorderLeft(BorderStyle.THIN);
        cellHeaderStyle.setBorderRight(BorderStyle.THIN);

        cell.setCellStyle(cellHeaderStyle);


        field.setAccessible(isAccessible);
    }

    public void createHeaderCell(Row row) {

        Workbook workbook = row.getSheet().getWorkbook();
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle cellHeaderStyle = workbook.createCellStyle();

        cellHeaderStyle.setBorderTop(BorderStyle.THIN);
        cellHeaderStyle.setBorderBottom(BorderStyle.THIN);
        cellHeaderStyle.setBorderLeft(BorderStyle.THIN);
        cellHeaderStyle.setBorderRight(BorderStyle.THIN);
        cellHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        cellHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellHeaderStyle.setFont(font);

        Cell cell = row.createCell(annotation_columnIndex);

        String[] splitHeader = annotation_headerName.split("¶");

        if(splitHeader.length>1){
            cell.setCellValue(splitHeader[splitHeader.length-1]);
        }else{
            cell.setCellValue(splitHeader[0]);
        }

        cell.setCellStyle(cellHeaderStyle);

    }

    public void createTitleCell(Row row,String titleName) {

        Workbook workbook = row.getSheet().getWorkbook();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight((short)400);
        CellStyle cellHeaderStyle = workbook.createCellStyle();

        cellHeaderStyle.setBorderTop(BorderStyle.THIN);
        cellHeaderStyle.setBorderBottom(BorderStyle.THIN);
        cellHeaderStyle.setBorderLeft(BorderStyle.THIN);
        cellHeaderStyle.setBorderRight(BorderStyle.THIN);
        cellHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
        cellHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellHeaderStyle.setFont(font);

        Cell cell = row.createCell(0);
        cell.setCellValue(titleName);
        cell.setCellStyle(cellHeaderStyle);


    }



    public void invokeObjectProperty(Cell cell, Object obj)
            throws Exception {
        boolean isAccessible = field.canAccess(obj);
        field.setAccessible(true);

        Class<?> fieldClass = field.getType();

        if(cell == null){
            Class<?> aClass = Class.forName(field.getType().getName());
            if(aClass.isEnum()){
                field.set(obj,null);
            }else{
                field.set(obj,"");
            }
        }else if (fieldClass == Integer.class || fieldClass == int.class) {
            int value = (int) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Short.class || fieldClass == short.class) {
            short value = (short) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Long.class || fieldClass == long.class) {
            long value = (long) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == String.class) {
            String value;
            if(NUMERIC.equals(cell.getCellType())){
                value = String.format(annotation_formatting,cell.getNumericCellValue());
            }else{
                value = cell.getStringCellValue();
            }
            field.set(obj, value);
        } else if (fieldClass == Double.class || fieldClass == double.class) {
            double value = cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Float.class || fieldClass == float.class) {
            float value = (float) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Byte.class || fieldClass == byte.class) {
            byte value = (byte) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Character.class || fieldClass == char.class) {
            char value = (char) cell.getNumericCellValue();
            field.set(obj, value);
        } else if (fieldClass == Boolean.class) {
            Boolean value =  cell.getBooleanCellValue();
            field.set(obj, value);
        } else if (fieldClass == Date.class) {
            Date value = dateFormat.parse(cell.getStringCellValue());
            field.set(obj, value);
        } else if (cell.getCellType() == CellType.STRING) {
            Class<?> aClass = Class.forName(field.getType().getName());
            if(aClass.isEnum()){
                enumAssign(cell, obj, aClass);
            }else{
                field.set(obj,cell.getStringCellValue());
            }

        }else if (cell.getCellType() == BLANK) {
            //pass
        }else {
            throw new RuntimeException(fieldClass + " is not supported.");
        }

        field.setAccessible(isAccessible);
    }

    private void enumAssign(Cell cell, Object obj, Class<?> aClass) {
        Object[] enumConstants = aClass.getEnumConstants();


        Stream.of(enumConstants).filter(str->String.valueOf(str).equals(cell.getStringCellValue()))
                .findFirst().ifPresent(
                        str-> {
                            try {
                                Field declaredField = aClass.getDeclaredField(String.valueOf(str));
                                field.set(obj, declaredField.get(aClass));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (NoSuchFieldException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    public int compareTo(CellTemplate obj) {
        if (this.annotation_columnIndex == obj.annotation_columnIndex) {
            return 0;
        }
        return this.annotation_columnIndex > obj.annotation_columnIndex ? 1 : -1;
    }

    public String getAnnotation_headerName() {
        return annotation_headerName;
    }

    public int getAnnotation_columnIndex() {
        return annotation_columnIndex;
    }

    public String getAnnotation_formatting() {
        return annotation_formatting;
    }
}