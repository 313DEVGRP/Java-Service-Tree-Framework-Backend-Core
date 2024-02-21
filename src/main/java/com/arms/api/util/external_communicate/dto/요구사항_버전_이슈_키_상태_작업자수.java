package com.arms.api.util.external_communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class 요구사항_버전_이슈_키_상태_작업자수 {

    private Long[] versionArr; //pdServiceVersions
    private String issueKey;   //이슈키

    private String statusName; //이슈상태
    private int numOfWorkers = 1; //기본값
}
