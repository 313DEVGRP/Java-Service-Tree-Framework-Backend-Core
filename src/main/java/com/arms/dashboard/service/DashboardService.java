package com.arms.dashboard.service;

import com.arms.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.dashboard.model.SankeyData;
import com.arms.dashboard.model.Worker;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_일반_집계_요청;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    검색결과_목록_메인 commonNestedAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    검색결과_목록_메인 commonFlatAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    Map<String, RequirementJiraIssueAggregationResponse> requirementsJiraIssueStatuses(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    SankeyData sankeyChartAPI(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception;

    List<Worker> 작업자별_요구사항_관여도(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청);

    Map<String, Long> 제품서비스별_담당자_이름_통계(Long pdServiceId);

    검색결과_목록_메인 제품서비스_일반_통계(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터);

    Map<String, Object> 제품서비스_요구사항제외_일반_통계(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터);

    List<Object> 제품서비스_요구사항제외_일반_통계_TOP_5(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터);

    Map<String, Object> getIssueResponsibleStatusTop5(Long pdServiceId, 지라이슈_일반_집계_요청 검색요청_데이터);
}
