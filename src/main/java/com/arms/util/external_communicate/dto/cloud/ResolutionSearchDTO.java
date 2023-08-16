package com.arms.util.external_communicate.dto.cloud;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionSearchDTO {

    private Integer maxResults;
    private Integer startAt;
    private Integer total;
    private boolean isLast;
    private List<Resolution> values;
}
