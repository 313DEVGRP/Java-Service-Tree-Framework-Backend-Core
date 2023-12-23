package com.arms.util.external_communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class 지라이슈_제품_및_제품버전_병합_검색_요청 {

    private 지라이슈_제품_및_제품버전_검색요청 요구_사항;
    private 지라이슈_제품_및_제품버전_검색요청 하위_이슈_사항;
}
