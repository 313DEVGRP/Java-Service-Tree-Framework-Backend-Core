package com.arms.util.external_communicate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 지라_이슈_유형_데이터_전송_객체 {

    // 온프레미스, 클라우드 공통
    private String self;
    private String id;
    private String description;
    private String name;
    private Boolean subtask;

    // 클라우드만 사용
    private String untranslatedName;
    private Integer hierarchyLevel;

}
