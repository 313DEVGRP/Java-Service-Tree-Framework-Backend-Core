package com.arms.util.external_communicate;

import com.arms.util.external_communicate.dto.cloud.CloudJiraIssueTypeDTO;
import com.arms.util.external_communicate.dto.cloud.CloudJiraPriorityDTO;
import com.arms.util.external_communicate.dto.cloud.CloudJiraProjectDTO;
import com.arms.util.external_communicate.dto.onpremise.*;
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

    @GetMapping("/{connectId}/onpremise/jira/issuetype/list")
    List<OnPremiseJiraIssueTypeDto> 지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/priority/list")
    List<OnPremiseJiraPriorityDTO> 지라_이슈_우선순위_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/resolution/list")
    List<OnPremiseJiraResolutionDTO> 지라_이슈_해결책_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/onpremise/jira/status")
    List<OnPremiseJiraStatusDTO> 지라_이슈_상태_가져오기(@PathVariable("connectId") String connectId); // return 확인.

    /*
     * 클라우드
     */
    @GetMapping("/{connectId}/cloud/jira/project/list")
    List<CloudJiraProjectDTO> 클라우드_지라_프로젝트_리스트_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuetype/list")
    List<CloudJiraIssueTypeDTO> 클라우드_지라_이슈_타입_가져오기(@PathVariable("connectId") String connectId);

    @GetMapping("/{connectId}/cloud/jira/issuepriority/list")
    List<CloudJiraPriorityDTO> 클라우드_지라_우선순위_가져오기(@PathVariable("connectId") String connectId);

}
