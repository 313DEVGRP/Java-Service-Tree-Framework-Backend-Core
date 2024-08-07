package com.arms.api.analysis.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregationRequestDTO {
    @NotNull private Long pdServiceLink;
    @NotEmpty private List<Long> pdServiceVersionLinks;
    @Builder.Default private IsReqType isReqType = IsReqType.ALL;
    private Boolean isReq;
    private String 메인_그룹_필드;
    private List<String> 하위_그룹_필드들;
    private boolean 컨텐츠_보기_여부;
    @Builder.Default private int 크기 = 1000;
    @Builder.Default private int 하위_크기 = 1000;

}
