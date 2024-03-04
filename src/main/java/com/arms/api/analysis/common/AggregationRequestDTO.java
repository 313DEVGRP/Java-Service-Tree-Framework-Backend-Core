package com.arms.api.analysis.common;

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
    private IsReqType isReqType = IsReqType.ALL;
    private Boolean isReq;
    private String 메인그룹필드;
    private List<String> 하위그룹필드들;
    private boolean 컨텐츠보기여부;
    private int 크기 = 1000;
    private int 하위크기 = 1000;

}
