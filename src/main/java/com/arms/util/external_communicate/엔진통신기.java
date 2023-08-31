package com.arms.util.external_communicate;

import com.arms.util.external_communicate.dto.*;
import com.arms.util.external_communicate.dto.cloud.*;
import com.arms.util.external_communicate.dto.onpremise.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(name = "engine", url = "${arms.engine.url}")
public interface 엔진통신기 {

    @PostMapping("/jira/connect/info")
    JiraInfoEntity 지라서버_등록(@RequestBody JiraInfoDTO jiraInfoDTO);

    /*
     * 공통
     */
    @PostMapping("/{connectId}/jira/issue")
    public 지라_이슈_데이터_전송_객체 이슈_생성하기(@PathVariable("connectId") Long 연결_아이디,
                                   @RequestBody 지라_이슈_생성_데이터_전송_객체 지라_이슈_생성_데이터_전송_객체);

    @GetMapping("/{connectId}/jira/project/list")
    public List<지라_프로젝트_데이터_전송_객체> 지라_프로젝트_목록_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuetype/list")
    public List<지라_이슈_유형_데이터_전송_객체> 지라_이슈_유형_가져오기 (@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuepriority/list")
    public List<지라_이슈_우선순위_데이터_전송_객체> 지라_이슈_우선순위_가져오기 (@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issueresolution/list")
    public List<지라_이슈_해결책_데이터_전송_객체> 지라_이슈_해결책_가져오기 (@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuestatus/list")
    public List<지라_이슈_상태_데이터_전송_객체> 지라_이슈_상태_가져오기(@PathVariable("connectId") String 연결_아이디);

    /*
     * 클라우드
     */
    @GetMapping("/{connectId}/jira/issuetype/project/{projectId}")
    public List<지라_이슈_유형_데이터_전송_객체> 클라우드_프로젝트별_이슈_유형_목록(@PathVariable("connectId") String 연결_아이디,
                                                                         @PathVariable("projectId") String 프로젝트_아이디);
    @GetMapping("/{connectId}/jira/issuestatus/project/{projectId}")
    public List<지라_이슈_상태_데이터_전송_객체> 클라우드_프로젝트별_이슈_상태_목록(@PathVariable("connectId") String 연결_아이디,
                                                                         @PathVariable("projectId") String 프로젝트_아이디);

    @GetMapping("/loadToES/bulk/{issueKey}")
    public int 이슈_검색엔진_벌크_저장( @PathVariable("connectId") Long 지라서버_아이디, @PathVariable("issueKey") String 이슈_키);

}
