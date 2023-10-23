package com.arms.util.external_communicate;

import com.arms.dashboard.model.combination.RequirementJiraIssueAggregationResponse;
import com.arms.dashboard.model.donut.AggregationResponse;
import com.arms.dashboard.model.sankey.SankeyElasticSearchData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "engine-dashboard", url = "${arms.engine.url}")
public interface 통계엔진통신기 {
    @GetMapping("/engine/jira/dashboard/jira-issue-statuses")
    public List<AggregationResponse> 제품_혹은_제품버전들의_지라이슈상태_집계(
            @RequestParam Long pdServiceLink,
            @RequestParam List<Long> pdServiceVersionLinks
    );

    @GetMapping("/engine/jira/dashboard/requirements-jira-issue-statuses")
    public Map<String, RequirementJiraIssueAggregationResponse> 제품_혹은_제품버전들의_요구사항_지라이슈상태_월별_집계(
            @RequestParam Long pdServiceLink,
            @RequestParam List<Long> pdServiceVersionLinks
    );

    @GetMapping("/engine/jira/dashboard/issue-assignee/{pdServiceId}")
    public Map<String, Long> 제품서비스별_담당자_이름_통계(@PathVariable("pdServiceId") Long 제품서비스_아이디);

    @GetMapping("/engine/jira/dashboard/version-assignee")
    public Map<String, List<SankeyElasticSearchData>> 제품_혹은_제품버전들의_담당자목록(
            @RequestParam Long pdServiceLink,
            @RequestParam List<Long> pdServiceVersionLinks
    );

}
