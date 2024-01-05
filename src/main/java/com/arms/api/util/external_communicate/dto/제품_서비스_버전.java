package com.arms.api.util.external_communicate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 제품_서비스_버전 {

    private Long 제품_서비스_버전;
    private List<요구_사항> 요구사항들;
}
