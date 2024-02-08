package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된지_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

public interface 비용서비스 {

    버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    요구사항목록_난이도_및_우선순위통계데이터 요구사항목록_난이도_및_우선순위통계_가져오기(ReqAddDTO reqAddDTO) throws Exception;
    버전별_요구사항별_연결된지_지라이슈데이터 버전별_요구사항에_연결된지_지라이슈(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception;
}
