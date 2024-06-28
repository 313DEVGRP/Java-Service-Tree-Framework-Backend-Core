package com.arms.api.util.communicate.external.request.aggregation;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class 지라이슈_일반_집계_요청 {

    private Boolean isReq;
    private List<String> 하위_그룹_필드들;
    private String 메인_그룹_필드;
    @Builder.Default private int 크기 = 1000;
    private boolean 컨텐츠_보기_여부;

}
