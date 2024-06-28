package com.arms.api.util.communicate.external.request.aggregation;

import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@AllArgsConstructor
public class 지라이슈_단순_집계_요청 {

    private List<String> 하위_그룹_필드들;
    private String 메인_그룹_필드;
    private boolean 컨텐츠_보기_여부;
    @Builder.Default private int 크기 = 1000;
    @Builder.Default private int 하위_크기 = 1000;
}
