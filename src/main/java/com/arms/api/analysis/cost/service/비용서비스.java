package com.arms.api.analysis.cost.service;

import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

import java.util.List;

public interface 비용서비스 {

    List<검색결과> 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);
}
