package com.arms.api.dashboard.service;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.dashboard.model.SankeyData;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    검색결과_목록_메인 commonNestedAggregation(AggregationRequestDTO aggregationRequestDTO);

    검색결과_목록_메인 commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO);

    Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(AggregationRequestDTO aggregationRequestDTO);

    SankeyData sankeyChartAPI(AggregationRequestDTO aggregationRequestDTO) throws Exception;

    List<Worker> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception;

    Map<String, Long> 제품서비스별_담당자_이름_통계(Long pdServiceId);

    검색결과_목록_메인 제품서비스_일반_통계(Long pdServiceId, AggregationRequestDTO aggregationRequestDTO);

    Map<String, Object> getIssueResponsibleStatusTop5(Long pdServiceId, AggregationRequestDTO aggregationRequestDTO);

    // 대시보드 상단 - 범위현황 (생성된 요구사항, 생성한 연결이슈)
    Map<String, Long> 대시보드_상단_요구사항_하위이슈_집계(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Object> 인력별_요구사항_top5(Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

    Map<String, Map<Long,Long>> 인력별_요구사항_상태_누적_Top5(String changeReqTableName, Long pdServiceId, List<Long> pdServiceVersionLinks) throws Exception;

}
