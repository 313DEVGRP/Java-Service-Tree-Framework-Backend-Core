package com.arms.egovframework.javaservice.treeframework.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtilsXlsx extends ExcelUtilsBase {

    public ExcelUtilsXlsx(InputStream inputStream) {
        super(inputStream);
    }

    public ExcelUtilsXlsx(OutputStream outputStream) {
        super(outputStream);
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
    public <T> void fillWorkBook(Workbook workbook, String sheetName, List<CellTemplate> templateList, List<T> srcContent)
            throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        XSSFSheet sheet = ((XSSFWorkbook) workbook).createSheet(sheetName);
        createHeaderRow(templateList, srcContent, sheet);
        createBodyRows(templateList, srcContent, sheet);
        createTitleRow(templateList, srcContent, sheet);

    }


    @Getter
    @AllArgsConstructor
    public static class HeaderRange {
        private Integer start;
        private Integer end;
    }


    @Getter
    public static class MergeHeaderBox {

        private final List<HeaderRange> headerRanges;
        private final List<CellTemplate> templateList;
        private final Integer startRow;

        public MergeHeaderBox(List<CellTemplate> templateList,int startRow) {
            this.headerRanges = getHeaderRanges(templateList);
            this.templateList = templateList;
            this.startRow = startRow;
        }

        public boolean hasNotHeader(Integer index){
            return headerRanges.stream()
                    .noneMatch(a->a.getEnd()>=index&&index>=a.getStart());
        }

        public List<CellRangeAddress> cellRangeAddresses(){
            return headerRanges.stream().map(a->new CellRangeAddress(startRow,startRow,a.getStart(),a.getEnd())).collect(
                    Collectors.toUnmodifiableList());
        }

        public List<CellRangeAddress> cellRangeAddressesDetail(){
            return templateList.stream().filter(a->hasNotHeader(a.getAnnotation_columnIndex()))
                    .map(a->new CellRangeAddress(startRow,startRow+1,a.getAnnotation_columnIndex(),a.getAnnotation_columnIndex())).collect(
                            Collectors.toUnmodifiableList());

        }

        public void setMergeRegions(XSSFSheet sheet){

            cellRangeAddresses().forEach(
                    sheet::addMergedRegion
            );

            if(headerRanges.size()>0){
                cellRangeAddressesDetail().forEach(
                        sheet::addMergedRegion
                );
            }
        }

        private List<HeaderRange> getHeaderRanges(List<CellTemplate> templateList) {

            List<Integer> indexLists = templateList.stream()
                    .filter(a -> a.getAnnotation_headerName().contains("¶"))
                    .map(CellTemplate::getAnnotation_columnIndex)
                    .collect(Collectors.toUnmodifiableList());

            int start = 0;
            int end = 0;

            List<HeaderRange> ranges = new ArrayList<>();

            for(int i = 0; i < indexLists.size(); i++){
                if(i == 0|| indexLists.get(i)- indexLists.get(i-1)!=1||
                        (
                                indexLists.get(i)- indexLists.get(i-1)==1
                                        &&(!templateList.get(indexLists.get(i)).getAnnotation_headerName().split("¶")[0].equals(
                                        templateList.get(indexLists.get(i-1)).getAnnotation_headerName().split("¶")[0]
                                )
                                )
                        )
                ){

                    if(end != 0){
                        ranges.add(new HeaderRange(start,end));
                    }
                    start = indexLists.get(i);
                }
                end = indexLists.get(i);
            }

            if (end != 0) {
                ranges.add(new HeaderRange(start,end));
            }

            return ranges;
        }
    }

    private <T> void createHeaderRow(List<CellTemplate> templateList, List<T> srcContent, XSSFSheet sheet) {

        int headerTotalRowSize = this.getHeaderTotalRowSize(srcContent.get(0).getClass());
        int headerTitleRowSize = this.getHeaderTitleRowSize(srcContent.get(0).getClass());

        MergeHeaderBox mergeHeaderBox = new MergeHeaderBox(templateList,headerTitleRowSize);

        IntStream.range(headerTitleRowSize, headerTotalRowSize).forEach(
                index->{
                    XSSFRow mergeHeaderRow = sheet.createRow(index);
                    for (CellTemplate template : templateList) {
                        template.createHeaderCell(mergeHeaderRow);
                    }
                }
        );

        mergeHeaderBox.setMergeRegions(sheet);

    }

    private <T> void createTitleRow(List<CellTemplate> templateList, List<T> srcContent, XSSFSheet sheet) {

        XSSFRow row = sheet.createRow(0);

        for (CellTemplate template : templateList) {
            template.createTitleCell(row,this.getHeaderTitleName(srcContent.get(0).getClass()));
        }

        sheet.addMergedRegion(new CellRangeAddress(0,0,0,templateList.size()-1));

    }

    private <T> void createBodyRows(List<CellTemplate> templateList, List<T> srcContent, XSSFSheet sheet)
            throws IllegalAccessException, ClassNotFoundException {

        int rowNum = this.getHeaderTotalRowSize(srcContent.get(0).getClass());

        for (T item : srcContent) {
            XSSFRow row = sheet.createRow(rowNum++);
            for (CellTemplate template : templateList) {
                template.createCell(row, item);
            }
        }


    }


}
