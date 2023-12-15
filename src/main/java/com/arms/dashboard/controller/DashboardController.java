package com.arms.dashboard.controller;

import com.arms.dashboard.model.combination.RequirementJiraIssueAggregationResponse;
import com.arms.dashboard.model.sankey.SankeyData;
import com.arms.dashboard.service.DashboardService;
import com.arms.util.external_communicate.dto.search.검색결과;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈_일반_검색_요청;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.CommonResponse.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import com.arms.util.external_communicate.*;


@Slf4j
@RestController
@RequestMapping(value = "/arms/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    static final long DUMMY_JIRA_SERVER_ID = 0L;

    private final 엔진통신기 엔진통신기;
    private final 통계엔진통신기 통계엔진통신기;
    private final DashboardService dashboardService;

    @GetMapping("/aggregation/nested")
    public ResponseEntity<ApiResult<검색결과_목록_메인>> commonNestedAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.commonNestedAggregation(지라이슈_제품_및_제품버전_검색요청)));
    }

    @GetMapping("/aggregation/flat")
    public ResponseEntity<ApiResult<검색결과_목록_메인>> commonFlatAggregation(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.commonFlatAggregation(지라이슈_제품_및_제품버전_검색요청)));
    }

    /**
     * C3 Donut Chart API - Dashboard
     */
    @GetMapping("/requirements-jira-issue-statuses")
    public ResponseEntity<ApiResult<Map<String, RequirementJiraIssueAggregationResponse>>> requirementsJiraIssueStatuses(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.requirementsJiraIssueStatuses(지라이슈_제품_및_제품버전_검색요청)));
    }

    /**
     * D3 Sankey Chart API - Dashboard, Analysis Resource
     */
    @GetMapping("/version-assignees")
    public ResponseEntity<ApiResult<SankeyData>> assigneesByPdServiceVersion(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.sankeyChartAPI(지라이슈_제품_및_제품버전_검색요청)));
    }

    /**
     * Apache Echarts TreeMap Chart API - Dashboard, Analysis Resource
     */
    @GetMapping("/assignees-requirements-involvements")
    ResponseEntity<ApiResult<List<Map<String, Object>>>> 작업자별_요구사항_관여도(
            지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    ) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(dashboardService.작업자별_요구사항_관여도(지라이슈_제품_및_제품버전_검색요청)));
    }

    @GetMapping(value = "/getVersionProgress")
    public ModelAndView getVersionProgress(HttpServletRequest request) {
        /* 임시 틀 생성 */
        String 제품서비스_아이디 = request.getParameter("pdserviceId");
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result",
                엔진통신기.제품서비스_버전별_상태값_통계(DUMMY_JIRA_SERVER_ID, 11L, 10L));

        return modelAndView;
    }

    @GetMapping(value = "/assignee-jira-issue-statuses")
    public ModelAndView getPerformancePerPersion(@RequestParam Long pdServiceLink) throws Exception {
        Map<String, Map<String, Map<String, Integer>>> 통신결과 = 통계엔진통신기.담당자_요구사항여부_상태별집계(pdServiceLink);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }


    @GetMapping(value = "/jira-issue-assignee")
    public ModelAndView getJiraAssigneeList(@RequestParam Long pdServiceId) {
        Map<String, Long> 통신결과 = 통계엔진통신기.제품서비스별_담당자_이름_통계(pdServiceId);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }


    @GetMapping("/normal/{pdServiceId}")
    public ModelAndView normalAggregation(@PathVariable("pdServiceId") Long pdServiceId, 지라이슈_일반_검색_요청 검색요청_데이터) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    @GetMapping("/exclusion-isreq-normal/{pdServiceId}")
    public ModelAndView exclusionIsReqNormalAggregation(@PathVariable("pdServiceId") Long pdServiceId, 지라이슈_일반_검색_요청 검색요청_데이터) {
        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.제품서비스_요구사항제외_일반_통계(pdServiceId, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        Map<String, Object> 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    @GetMapping("/exclusion-isreq-normal/req-and-linked-issue-top5/{pdServiceId}")
    public ModelAndView getReqAndLinkedIssueTop5(@PathVariable("pdServiceId") Long pdServiceId, 지라이슈_일반_검색_요청 검색요청_데이터) {
        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.제품서비스_요구사항제외_일반_통계(pdServiceId, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        Map<String, Object> 통신결과 = 요구사항_연결이슈_일반_통계.getBody();

        Map<String, Object> 검색결과 = (Map<String, Object>) 통신결과.get("검색결과");
        List<Object> 작업자별결과 = (List<Object>) 검색결과.get("group_by_assignee.assignee_emailAddress.keyword");
        modelAndView.addObject("result", 작업자별결과);
        return modelAndView;
    }

    @GetMapping("/normal/issue-responsible-status-top5/{pdServiceId}")
    public ModelAndView getIssueResponsibleStatusTop5(@PathVariable("pdServiceId") Long pdServiceId, 지라이슈_일반_검색_요청 검색요청_데이터) {
        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.제품서비스_일반_통계(pdServiceId, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 검색결과목록 = 요구사항_연결이슈_일반_통계.getBody();
        List<검색결과> 작업자별결과 = 검색결과목록.get검색결과().get("group_by_assignee.assignee_emailAddress.keyword");

        Map<String, Object> personAndStatus = new HashMap<>();
        for (검색결과 obj : 작업자별결과) {
            String 작업자메일 = obj.get필드명();
            int 엣위치 = 작업자메일.indexOf("@");
            String 작업자아이디 = 작업자메일.substring(0, 엣위치);
            Map<String, List<검색결과>> 하위검색_이슈상태 = (Map<String, List<검색결과>>) obj.get하위검색결과();//("group_by_assignee.assignee_emailAddress.keyword");
            personAndStatus.put(작업자아이디, 하위검색_이슈상태);
        }

        modelAndView.addObject("result", personAndStatus);
        return modelAndView;
    }

}
