package com.arms.util.external_communicate.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class 지라이슈_검색_서브버킷_요청 {

    private Long 서비스아이디;
    private String 특정필드;
    private String 특정필드검색어;
    private String 그룹할필드;
    private String 하위_그룹할필드;
    private int size;
    private boolean historyView;
    private boolean isReq;
}
