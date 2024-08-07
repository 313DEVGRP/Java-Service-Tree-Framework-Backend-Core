package com.arms.api.analysis.cost.model;

import com.arms.egovframework.javaservice.treeframework.excel.ExcelClassAnnotation;
import com.arms.egovframework.javaservice.treeframework.excel.ExcelFieldAnnotation;
import lombok.*;
import org.hibernate.annotations.*;

@Getter
@Setter
@Builder
@SelectBeforeUpdate(value=true)
@DynamicInsert(value=true)
@DynamicUpdate(value=true)
@Cache(usage = CacheConcurrencyStrategy.NONE)
@ExcelClassAnnotation(sheetName = "Sheet1", headerRowSize = 1, headerTitleName = "[샘플] 인력별 연봉 데이터 템플릿")
@AllArgsConstructor
@NoArgsConstructor
public class SampleDTO {


    @ExcelFieldAnnotation(columnIndex = 0, formatting = "%.0f", headerName = "번호")
    private String num;

    @ExcelFieldAnnotation(columnIndex = 1, formatting = "%.0f", headerName = "항목")
    private String category;

    @ExcelFieldAnnotation(columnIndex = 2, formatting = "%.0f", headerName = "설명")
    private String desc;

    @ExcelFieldAnnotation(columnIndex = 3, formatting = "%.0f", headerName = "예시")
    private String example;

    @ExcelFieldAnnotation(columnIndex = 4, formatting = "%.0f", headerName = "기타")
    private String etc;

}