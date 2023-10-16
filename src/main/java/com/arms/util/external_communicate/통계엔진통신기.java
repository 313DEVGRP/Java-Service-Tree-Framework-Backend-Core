package com.arms.util.external_communicate;

import com.arms.dashboard.model.AggregationResponse;
import com.arms.dashboard.model.RequirementJiraIssueAggregationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "dashboardEngine", url = "${arms.engine.url}")
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


}
