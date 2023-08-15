package com.arms.외부연동;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnPremiseJiraProjectDTO {

    private String self;

    private String id;

    private String key;

    private String name;

}