package com.arms.외부연동;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "engine", url = "${arms.engine.url}")
public interface 엔진통신기 {

    @PostMapping("/jira/connect/info")
    JiraInfoEntity 지라서버_등록(@RequestBody JiraInfoDTO jiraInfoDTO);

    /*
     * 온프레미스
     */
    @GetMapping("/{connectId}/onpremise/jira/project/list")
    List<OnPremiseJiraProjectDTO> 지라_프로젝트_리스트_가져오기(@PathVariable("connectId") String connectId);

    // 이슈타입
    @GetMapping("/{connectId}/onpremise/jira/issuetype/list")
    List<IssueType> 지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);




    /*
     * 클라우드
     */
    @GetMapping("/{connectId}/cloud/jira/project/list")
    List<CloudJiraProjectDTO> 클라우드_지라_프로젝트_리스트_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuetype/list")
    List<IssueType> 클라우드_지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);
}
