package com.arms.util.external_communicate.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class 지라이슈_일자별_제품_및_제품버전_검색요청 extends 지라이슈_제품_및_제품버전_검색요청 {
    private String 일자기준;
    private int 날짜페이지;
    private int 날짜크기;
}