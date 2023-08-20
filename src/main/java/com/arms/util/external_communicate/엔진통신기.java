package com.arms.util.external_communicate;

import com.arms.util.external_communicate.dto.cloud.*;
import com.arms.util.external_communicate.dto.onpremise.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "engine", url = "${arms.engine.url}")
public interface 엔진통신기 {

    @PostMapping("/jira/connect/info")
    JiraInfoEntity 지라서버_등록(@RequestBody JiraInfoDTO jiraInfoDTO);

    /*
     * 온프레미스
     */
    @GetMapping("/{connectId}/onpremise/jira/issue")
    List<OnPremiseJiraProjectDTO> 온프라미스에_요구사항이슈_생성하기(@PathVariable("connectId") String connectId, @RequestBody OnPremiseJiraIssueInputDTO onPremiseJiraIssueInputDTO);

    @GetMapping("/{connectId}/onpremise/jira/project/list")
    List<OnPremiseJiraProjectDTO> 지라_프로젝트_리스트_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/issuetype/list")
    List<OnPremiseJiraIssueTypeDto> 지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/issuepriority/list")
    List<OnPremiseJiraPriorityDTO> 지라_이슈_우선순위_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/issueresolution/list")
    List<OnPremiseJiraResolutionDTO> 지라_이슈_해결책_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/issuestatus/list")
    List<OnPremiseJiraStatusDTO> 지라_이슈_상태_가져오기(@PathVariable("connectId") String connectId); // return 확인.

    /*
     * 클라우드
     */
    @GetMapping("/{connectId}/cloud/jira/issue")
    CloudJiraIssueDTO 클라우드에_요구사항이슈_생성하기(@PathVariable("connectId") String connectId, @RequestBody CloudJiraIssueInputDTO cloudJiraIssueInputDTO);

    @GetMapping("/{connectId}/cloud/jira/project/list")
    List<CloudJiraProjectDTO> 클라우드_지라_프로젝트_리스트_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuetype/list")
    List<CloudJiraIssueTypeDTO> 클라우드_지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuepriority/list")
    PrioritySearchDTO 클라우드_지라_우선순위_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issueresolution/list")
    ResolutionSearchDTO 클라우드_지라_해결책_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuestatus/list")
    StatusSearchDTO 클라우드_지라_상태_가져오기(@PathVariable("connectId") String connectId);




}
