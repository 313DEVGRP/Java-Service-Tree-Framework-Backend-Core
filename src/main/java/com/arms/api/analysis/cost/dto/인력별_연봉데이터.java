package com.arms.api.analysis.cost.dto;

import com.arms.egovframework.javaservice.treeframework.excel.ExcelClassAnnotation;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelFieldAnnotation;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ExcelClassAnnotation(sheetName = "Sheet1", headerRowSize = 1, headerTitleName = "연봉 정보")
public class 인력별_연봉데이터 {

    @ExcelFieldAnnotation(columnIndex = 0, formatting = "%0.f", headerName = "이름")
    private String 이름;

    @ExcelFieldAnnotation(columnIndex = 1, formatting = "%.0f", headerName = "고유 키")
    private String 키;

    @ExcelFieldAnnotation(columnIndex = 2, formatting = "%.0f", headerName = "연봉(만원)")
    private String 연봉;

}