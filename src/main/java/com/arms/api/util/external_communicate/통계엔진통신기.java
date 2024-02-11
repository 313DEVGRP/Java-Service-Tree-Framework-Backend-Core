package com.arms.api.util.external_communicate;

import com.arms.api.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.api.dashboard.model.RequirementJiraIssueAggregationResponse;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.util.external_communicate.dto.*;
import com.arms.api.util.external_communicate.dto.search.검색결과;
import com.arms.api.util.external_communicate.dto.search.검색결과_목록_메인;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@FeignClient(name = "engine-dashboard", url = "${arms.engine.url}")
public interface 통계엔진통신기 {

    @GetMapping("/engine/jira/dashboard/aggregation/nested")
    ResponseEntity<검색결과_목록_메인> 제품_혹은_제품버전들의_집계_nested(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    );

    @GetMapping("/engine/jira/dashboard/aggregation/flat")
    ResponseEntity<검색결과_목록_메인> 제품_혹은_제품버전들의_집계_flat(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    );

    @GetMapping("/engine/jira/dashboard/requirements-jira-issue-statuses")
    ResponseEntity<Map<String, RequirementJiraIssueAggregationResponse>> 제품_혹은_제품버전들의_요구사항_지라이슈상태_월별_집계(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    );

    @GetMapping("/engine/jira/dashboard/version-assignees")
    ResponseEntity<List<검색결과>> 제품_혹은_제품버전들의_담당자목록(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    );

    @PostMapping("/engine/jira/dashboard/assignees-requirements-involvements")
    ResponseEntity<List<Worker>> 작업자별_요구사항_관여도(
            @RequestBody 트리맵_검색요청 트리맵_검색요청
    );

    @GetMapping("/engine/jira/dashboard/issue-assignee/{pdServiceId}")
    Map<String, Long> 제품서비스별_담당자_이름_통계(@PathVariable("pdServiceId") Long 제품서비스_아이디);

    @GetMapping("/engine/jira/dashboard/assignee-jira-issue-statuses")
    Map<String, Map<String, Map<String, Integer>>> 담당자_요구사항여부_상태별집계(
            @RequestParam Long pdServiceLink) throws IOException;

    @GetMapping("/engine/jira/dashboard/normal/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 제품서비스_일반_통계(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @SpringQueryMap 지라이슈_일반_집계_요청 지라이슈_검색_일반_요청
    );

    @GetMapping("/engine/jira/dashboard/isreq-normal/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 제품서비스_일반_통계_개선(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @SpringQueryMap 지라이슈_일반_집계_요청 지라이슈_검색_일반_요청
    );

    @GetMapping("/engine/jira/dashboard/exclusion-isreq-normal/{pdServiceId}")
    ResponseEntity<Map<String, Object>> 제품서비스_요구사항제외_일반_통계(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @SpringQueryMap 지라이슈_일반_집계_요청 지라이슈_검색_일반_요청);

    @GetMapping("/engine/jira/dashboard/normal-version/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 제품서비스_일반_버전_통계(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @RequestParam List<Long> pdServiceVersionLinks,
            @SpringQueryMap 지라이슈_일반_집계_요청 지라이슈_검색_일반_요청);

    @GetMapping("/engine/jira/dashboard/normal-version-only/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 일반_버전필터_검색(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @RequestParam List<Long> pdServiceVersionLinks,
            @SpringQueryMap 지라이슈_단순_집계_요청 지라이슈_단순_집계_요청);

    @GetMapping("/engine/jira/dashboard/normal-versionAndMail-filter/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 일반_버전_및_작업자_필터_검색(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @RequestParam List<Long> pdServiceVersionLinks,
            @RequestParam List<String> mailAddressList,
            @SpringQueryMap 지라이슈_단순_집계_요청 지라이슈_단순_집계_요청);

    @GetMapping("/engine/jira/dashboard/standard-daily/jira-issue")
    ResponseEntity<Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터>> 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(
            @SpringQueryMap 지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청
    );

    @GetMapping("/engine/jira/dashboard/standard-daily/updated-jira-issue")
    ResponseEntity<List<지라이슈>> 기준일자별_제품_및_제품버전목록_업데이트된_이슈조회 (
            @SpringQueryMap 지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청);

    @GetMapping("/engine/jira/dashboard/standard-daily/updated-ridgeline")
    ResponseEntity<Map<Long, Map<String, Map<String,List<지라이슈>>>>> 기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회 (
            @SpringQueryMap 지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청
    );

    @GetMapping("/engine/jira/dashboard/normal-version/resolution/{pdServiceId}")
    ResponseEntity<검색결과_목록_메인> 제품서비스_일반_버전_해결책유무_통계(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청,
            @RequestParam String resolution
    );

    @PostMapping("/engine/jira/dashboard/req-status-and-reqInvolved-unique-assignees")
    ResponseEntity<List<제품_서비스_버전>> 요구사항_별_상태_및_관여_작업자_수(@RequestBody 지라이슈_제품_및_제품버전_병합_집계_요청 지라이슈_제품_및_제품버전_검색요청);

    @GetMapping("/engine/jira/dashboard/requirement-linkedissue/{pdServiceId}")
    ResponseEntity<List<지라이슈>> 제품별_요구사항_연결이슈_조회(
            @PathVariable("pdServiceId") Long 제품서비스_아이디,
            @RequestParam List<Long> pdServiceVersionLinks,
            @SpringQueryMap 지라이슈_일반_검색_요청 지라이슈_일반_검색_요청);

    @GetMapping("/engine/jira/dashboard/version-req-assignees")
    ResponseEntity<List<검색결과>> 제품별_버전_및_요구사항별_작업자(
            @SpringQueryMap 지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    );
}
