package com.arms.api.util.communicate.external.response.jira;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class 지라이슈우선순위_데이터 extends ALM_데이터 {


    // 온프레미스, 클라우드 공통
    private String name;
    private String description;

    // 클라우드
    private boolean isDefault;

}
