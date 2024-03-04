package com.arms.api.util.external_communicate.dto;

import com.arms.api.analysis.common.IsReqType;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class 지라이슈_일반_검색_요청 {

    private IsReqType isReqType;
    private List<Long> pdServiceVersionLinks;
    private Boolean isReq;
    private int 크기 = 1000;

}
