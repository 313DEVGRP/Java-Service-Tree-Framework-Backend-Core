package com.arms.api.analysis.dashboard.service;

import com.arms.api.analysis.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.analysis.dashboard.model.SankeyData;
import com.arms.api.analysis.dashboard.model.Worker;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    검색결과_목록_메인 commonNestedAggregation(EngineAggregationRequestDTO engineAggregationRequestDTO);

    검색결과_목록_메인 commonFlatAggregation(EngineAggregationRequestDTO engineAggregationRequestDTO);

    Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(EngineAggregationRequestDTO engineAggregationRequestDTO);

    SankeyData sankeyChartAPI(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception;

    List<Worker> 작업자별_요구사항_관여도(EngineAggregationRequestDTO engineAggregationRequestDTO) throws Exception;

    Map<String, Long> 제품서비스별_담당자_이름_통계(Long pdServiceId);

    검색결과_목록_메인 제품서비스_일반_통계(Long pdServiceId, EngineAggregationRequestDTO engineAggregationRequestDTO);

    Map<String, Object> 제품서비스_요구사항제외_일반_통계(Long pdServiceId, EngineAggregationRequestDTO engineAggregationRequestDTO);

    List<Object> 제품서비스_요구사항제외_일반_통계_TOP_5(Long pdServiceId, EngineAggregationRequestDTO engineAggregationRequestDTO);

    Map<String, Object> getIssueResponsibleStatusTop5(Long pdServiceId, EngineAggregationRequestDTO engineAggregationRequestDTO);
}
