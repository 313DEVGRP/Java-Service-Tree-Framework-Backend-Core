package com.arms.api.util.communicate.external.response.jira;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class 지라이슈상태_데이터 {

    private  String self;

    private String id;

    private String name;

    private String description;

}
