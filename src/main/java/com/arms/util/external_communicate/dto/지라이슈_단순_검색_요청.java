package com.arms.util.external_communicate.dto;

import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class 지라이슈_단순_검색_요청 {

    private List<String> 하위그룹필드들;
    private String 메인그룹필드;
    private int 크기 = 1000;
    private int 하위크기 = 1000;
    private boolean 컨텐츠보기여부;
}
