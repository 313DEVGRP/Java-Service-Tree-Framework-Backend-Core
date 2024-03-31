package com.arms.api.analysis.cost.service;

import com.arms.api.analysis.cost.dto.버전별_요구사항별_연결된_지라이슈데이터;
import com.arms.api.analysis.cost.dto.버전요구사항별_담당자데이터;
import com.arms.api.analysis.cost.dto.요구사항목록_난이도_및_우선순위통계데이터;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일반_집계_요청;

import java.util.List;
import java.util.Map;

public interface 비용서비스 {

    버전요구사항별_담당자데이터 전체_담당자가져오기(Long 제품아이디, List<Long> 버전아이디_목록, 지라이슈_일반_집계_요청 일반집계요청);

    버전요구사항별_담당자데이터 버전별_요구사항별_담당자가져오기(EngineAggregationRequestDTO engineAggregationRequestDTO);

    요구사항목록_난이도_및_우선순위통계데이터 요구사항목록_난이도_및_우선순위통계_가져오기(ReqAddDTO reqAddDTO) throws Exception;

    버전별_요구사항별_연결된_지라이슈데이터 버전별_요구사항_연결된_지라이슈키(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception;

    Map<String, Long> calculateInvestmentPerformance(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception;

    Long 연봉총합(Long pdServiceLink, List<Long> pdServiceVersionLinks) throws Exception;

    void v2(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception;
}
