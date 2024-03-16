package com.arms.api.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReqLinkDTO {
    private String connectInfo;
    private Long reqLink;
}
