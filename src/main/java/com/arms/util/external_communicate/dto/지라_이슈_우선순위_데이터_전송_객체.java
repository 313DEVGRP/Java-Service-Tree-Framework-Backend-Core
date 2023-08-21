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
public class 지라_이슈_우선순위_데이터_전송_객체 {


    // 온프레미스, 클라우드 공통
    private String self;
    private String id;
    private String name;
    private String description;

    // 클라우드
    private boolean isDefault;

}
