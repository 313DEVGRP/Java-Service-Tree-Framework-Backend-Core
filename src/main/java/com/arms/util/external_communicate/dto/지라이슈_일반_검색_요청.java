package com.arms.util.external_communicate.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
public class 지라이슈_일반_검색_요청 {

    private IsReqType isReqType;
    private List<Long> pdServiceVersionLinks;

}
