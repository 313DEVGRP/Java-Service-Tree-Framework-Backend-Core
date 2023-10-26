package com.arms.dashboard.controller;

import com.arms.dashboard.model.combination.RequirementJiraIssueAggregationResponse;
import com.arms.dashboard.model.donut.AggregationResponse;
import com.arms.dashboard.model.sankey.SankeyElasticSearchData;
import com.arms.dashboard.model.sankey.SankeyData;
import com.arms.dashboard.model.sankey.SankeyLink;
import com.arms.dashboard.model.sankey.SankeyNode;
import com.arms.globaltreemap.controller.TreeMapAbstractController;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.util.external_communicate.dto.지라이슈_검색_서브버킷_요청;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import com.arms.util.external_communicate.*;
import com.arms.util.external_communicate.dto.지라이슈_검색_일반_요청;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = "/arms/dashboard")
public class DashboardController extends TreeMapAbstractController {

    @Autowired
    private 엔진통신기 엔진통신기;

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final long dummy_jira_server = 0L;

    @GetMapping(value = "/getVersionProgress")
    @ResponseBody
    public ModelAndView getVersionProgress(HttpServletRequest request) throws Exception {
        /* 임시 틀 생성 */
        String 제품서비스_아이디 = request.getParameter("pdserviceId");
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result",
                엔진통신기.제품서비스_버전별_상태값_통계(dummy_jira_server, 11L, 10L));

        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value="/assignee-jira-issue-statuses")
    public ModelAndView getPerformancePerPersion(@RequestParam Long pdServiceLink) throws Exception {
        Map<String, Map<String, Map<String, Integer>>> 통신결과 = 통계엔진통신기.담당자_요구사항여부_상태별집계(pdServiceLink);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value="/jira-linkedIssue-subTask")
    public ModelAndView getLinkedIssueAndSubTask(@RequestParam Long pdServiceId) {
        log.info("DashboardController :: getLinkedIssueAndSubTask.pdServiceId ==> {}" , pdServiceId);
        지라이슈_검색_서브버킷_요청 검색요청_데이터 = 지라이슈_검색_서브버킷_요청.builder()
                        .그룹할필드("pdServiceVersion")
                        .서비스아이디(pdServiceId)
                        .하위_그룹할필드("parentReqKey")
                        .size(100)
                        .build();

        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_하위이슈_통계 = 엔진통신기.요구사항_연결이슈_하위이슈_통계(dummy_jira_server, 검색요청_데이터);
        log.info("DashboardController :: getLinkedIssueAndSubTask.pdServiceId ==> {}" , pdServiceId);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        Map<String, Object> 통신결과 = 요구사항_연결이슈_하위이슈_통계.getBody();
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value="/normal/jira-linkedIssue-subTask")
    public ModelAndView 연결이슈_하위이슈_가져오기(@RequestParam Long pdServiceId) {
        log.info("DashboardController :: getLinkedIssueAndSubTask.pdServiceId ==> {}" , pdServiceId);

        지라이슈_검색_일반_요청 검색요청_데이터 = 지라이슈_검색_일반_요청.builder()
                .메인그룹필드("pdServiceVersion")
                .서비스아이디(pdServiceId) // 2번들어가는중. 향후 수정 예정.
                .요구사항인지여부(false)
                .하위그룹필드들(List.of("parentReqKey"))
                .build();

        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_하위이슈_통계 = 엔진통신기.제품서비스_일반_검색(pdServiceId, 검색요청_데이터);
        log.info("DashboardController :: getLinkedIssueAndSubTask.pdServiceId ==> {}" , pdServiceId);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        Map<String, Object> 통신결과 = 요구사항_연결이슈_하위이슈_통계.getBody();
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value="/jira-issue-assignee")
    public ModelAndView getJiraAssigneeList(@RequestParam Long pdServiceId) {
        Map<String, Long> 통신결과 = 통계엔진통신기.제품서비스별_담당자_이름_통계(pdServiceId);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }


    @ResponseBody
    @GetMapping("/jira-issue-statuses")
    public ModelAndView jiraIssueStatuses(@RequestParam Long pdServiceLink, @RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("DashboardController :: jiraIssueStatuses");
        List<AggregationResponse> result = 통계엔진통신기.제품_혹은_제품버전들의_지라이슈상태_집계(pdServiceLink, pdServiceVersionLinks);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/requirements-jira-issue-statuses")
    public ModelAndView requirementsJiraIssueStatuses(@RequestParam Long pdServiceLink, @RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("DashboardController :: requirementsJiraIssueStatuses");
        Map<String, RequirementJiraIssueAggregationResponse> result = 통계엔진통신기.제품_혹은_제품버전들의_요구사항_지라이슈상태_월별_집계(pdServiceLink, pdServiceVersionLinks);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/version-assignees", method = RequestMethod.GET)
    public ModelAndView assigneesByPdServiceVersion(
            @RequestParam Long pdServiceLink,
            @RequestParam List<Long> pdServiceVersionLinks
    ) throws Exception {
        log.info("DashboardController :: getSankeyChart");

        if (pdServiceVersionLinks.isEmpty()) {
            return new ModelAndView("jsonView")
                    .addObject("result", new SankeyData(Collections.emptyList(), Collections.emptyList()));
        }

        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(pdServiceLink);
        PdServiceEntity savedPdService = pdService.getNode(pdServiceEntity);

        List<SankeyNode> nodeList = new ArrayList<>();
        List<SankeyLink> linkList = new ArrayList<>();

        String pdServiceId = savedPdService.getC_id() + "-product";
        nodeList.add(new SankeyNode(pdServiceId, savedPdService.getC_title(), "제품"));

        savedPdService.getPdServiceVersionEntities().stream()
                .filter(version -> pdServiceVersionLinks.contains(version.getC_id()))
                .sorted(Comparator.comparing(PdServiceVersionEntity::getC_id))
                .forEach(version -> {
                    String versionId = version.getC_id() + "-version";
                    nodeList.add(new SankeyNode(versionId, version.getC_title(), "버전"));
                    linkList.add(new SankeyLink(pdServiceId, versionId));
                });

        Map<String, List<SankeyElasticSearchData>> esData = 통계엔진통신기.제품_혹은_제품버전들의_담당자목록(pdServiceLink, pdServiceVersionLinks);

        esData.forEach((versionId, sankeyCharts) -> {
            sankeyCharts.stream().forEach(sankeyElasticSearchData -> {
                String assigneeAccountId = sankeyElasticSearchData.getAssigneeAccountId();
                String assigneeDisplayName = sankeyElasticSearchData.getAssigneeDisplayName();
                String nodeName = String.format("%s(%s)", assigneeDisplayName, assigneeAccountId);
                String workerNodeId = versionId + "-" + assigneeAccountId;
                nodeList.add(new SankeyNode(workerNodeId, nodeName, "작업자"));
                linkList.add(new SankeyLink(versionId + "-version", workerNodeId));
            });
        });

        SankeyData sankeyData = new SankeyData(nodeList, linkList);

        return new ModelAndView("jsonView")
                .addObject("result", sankeyData);
    }

    @ResponseBody
    @GetMapping("/normal/{pdServiceId}")
    public ModelAndView normal(@PathVariable("pdServiceId") Long pdServiceId, 지라이슈_검색_일반_요청 검색요청_데이터) throws Exception {

        log.info("DashboardController :: getLinkedIssueAndSubTask.pdServiceId ==> {}" , pdServiceId);

        ResponseEntity<Map<String, Object>> 요구사항_연결이슈_일반_통계
            = 통계엔진통신기.요구사항_연결이슈_일반_통계(pdServiceId, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        Map<String, Object> 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }


}
