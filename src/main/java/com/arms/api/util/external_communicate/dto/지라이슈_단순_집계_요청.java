package com.arms.api.util.external_communicate.dto;

import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@AllArgsConstructor
public class 지라이슈_단순_집계_요청 extends 기본_검색_요청{

    private List<String> 하위그룹필드들;
    private String 메인그룹필드;
    private boolean 컨텐츠보기여부;
}
