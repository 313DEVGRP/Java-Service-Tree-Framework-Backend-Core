package com.arms.util.external_communicate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 요구_사항 {

    private String 요구_사항_번호;
    private String 요구_사항_담당자;
    private int 작업자수;
    private List<String> 하위_이슈_사항_담당자;

}
