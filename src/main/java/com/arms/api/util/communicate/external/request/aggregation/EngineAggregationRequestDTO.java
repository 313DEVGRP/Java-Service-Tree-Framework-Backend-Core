package com.arms.api.util.communicate.external.request.aggregation;

import com.arms.api.analysis.common.IsReqType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EngineAggregationRequestDTO {
    private Boolean isReq;
    private Long pdServiceLink;
    private List<Long> pdServiceVersionLinks;
    private IsReqType isReqType = IsReqType.ALL;
    private String 메인그룹필드;
    private List<String> 하위그룹필드들;
    private boolean 컨텐츠보기여부;
    private int 크기 = 1000;
    private int 하위크기 = 1000;
    private List<Long> cReqLinks;

}
