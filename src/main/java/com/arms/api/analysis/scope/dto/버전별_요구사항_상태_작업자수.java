package com.arms.api.analysis.scope.dto;

import com.arms.api.util.external_communicate.dto.요구사항_버전_이슈_키_상태_작업자수;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class 버전별_요구사항_상태_작업자수 {

    private String 버전_문자열;

    private List<요구사항_버전_이슈_키_상태_작업자수> 요구사항_이슈_키_상태_작업자수_목록;

}
