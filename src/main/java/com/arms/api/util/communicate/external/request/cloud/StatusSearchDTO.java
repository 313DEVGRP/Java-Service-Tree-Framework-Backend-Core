package com.arms.api.util.communicate.external.request.cloud;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusSearchDTO {

    private Integer maxResults;
    private Integer startAt;
    private Integer total;
    private boolean isLast;
    private List<Status> values;
}
