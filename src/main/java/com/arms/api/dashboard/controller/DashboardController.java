package com.arms.api.dashboard.controller;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.dashboard.model.SankeyData;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.dashboard.service.DashboardService;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/arms/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/aggregation/nested")
    public ResponseEntity<ApiResult<검색결과_목록_메인>> commonNestedAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.commonNestedAggregation(aggregationRequestDTO)));
    }

    @GetMapping("/aggregation/flat")
    public ResponseEntity<ApiResult<검색결과_목록_메인>> commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.commonFlatAggregation(aggregationRequestDTO)));
    }

    /**
     * C3 Donut Chart API - Dashboard
     */
    @GetMapping("/requirements-jira-issue-statuses")
    public ResponseEntity<ApiResult<Map<String, RequirementJiraIssueAggregationResponse>>> requirementsJiraIssueStatuses(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.requirementsJiraIssueStatuses(aggregationRequestDTO)));
    }

    /**
     * D3 Sankey Chart API - Dashboard, Analysis Resource
     */
    @GetMapping("/version-assignees")
    public ResponseEntity<ApiResult<SankeyData>> assigneesByPdServiceVersion(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.sankeyChartAPI(aggregationRequestDTO)));
    }

    /**
     * TreeMap Chart API - Dashboard, Analysis Resource
     */
    @GetMapping("/assignees-requirements-involvements")
    ResponseEntity<ApiResult<List<Worker>>> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.작업자별_요구사항_관여도(aggregationRequestDTO)));
    }

    /**
     * ReqStatus, Dashboard
     */
    @GetMapping("/jira-issue-assignee")
    ResponseEntity<ApiResult<Map<String, Long>>> getJiraAssigneeList(@RequestParam Long pdServiceId) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.제품서비스별_담당자_이름_통계(pdServiceId)));
    }

    /**
     * Dashboard
     */
    @GetMapping("/normal/{pdServiceId}")
    ResponseEntity<ApiResult<검색결과_목록_메인>> normalAggregation(@PathVariable("pdServiceId") Long pdServiceId, AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.제품서비스_일반_통계(pdServiceId, aggregationRequestDTO)));
    }

    /**
     * Dashboard
     */
    @GetMapping("/exclusion-isreq-normal/req-and-linked-issue-top5/{pdServiceId}")
    ResponseEntity<ApiResult<List<Object>>> getReqAndLinkedIssueTop5(@PathVariable("pdServiceId") Long pdServiceId, AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.제품서비스_요구사항제외_일반_통계_TOP_5(pdServiceId, aggregationRequestDTO)));
    }

    /**
     * Dashboard
     */
    @GetMapping("/normal/issue-responsible-status-top5/{pdServiceId}")
    ResponseEntity<ApiResult<Map<String, Object>>> getIssueResponsibleStatusTop5(@PathVariable("pdServiceId") Long pdServiceId, AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.getIssueResponsibleStatusTop5(pdServiceId, aggregationRequestDTO)));
    }

}
