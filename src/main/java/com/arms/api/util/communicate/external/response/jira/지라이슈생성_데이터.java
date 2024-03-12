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
public class 지라이슈생성_데이터 {

    private 지라이슈필드_데이터 fields;

}
