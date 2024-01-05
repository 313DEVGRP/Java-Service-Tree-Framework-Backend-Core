package com.arms.api.util.external_communicate;

import com.arms.api.util.external_communicate.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "engine", url = "${arms.engine.url}")
public interface 엔진통신기 {

    //@PostMapping("/jira/connect/info")
    @PostMapping("/engine/serverinfo")
    지라서버정보_엔티티 지라서버_등록(@RequestBody 지라서버정보_데이터 서버정보데이터);

    /*
     * 공통
     */
    @PostMapping("/{connectId}/jira/issue")
    public 지라이슈_데이터 이슈_생성하기(@PathVariable("connectId") Long 연결_아이디,
                            @RequestBody 지라이슈생성_데이터 지라이슈생성_데이터);

    @GetMapping("/{connectId}/jira/project/list")
    public List<지라프로젝트_데이터> 지라_프로젝트_목록_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuetype/list")
    public List<지라이슈유형_데이터> 지라_이슈_유형_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuepriority/list")
    public List<지라이슈우선순위_데이터> 지라_이슈_우선순위_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issueresolution/list")
    public List<지라이슈해결책_데이터> 지라_이슈_해결책_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/{connectId}/jira/issuestatus/list")
    public List<지라이슈상태_데이터> 지라_이슈_상태_가져오기(@PathVariable("connectId") String 연결_아이디);

    @GetMapping("/engine/jira/{connectId}/issue/search/{issueKey}/subAndLinks")
    public List<지라이슈> 지라_연결된이슈_서브테스크_가져오기(@PathVariable("connectId") Long 연결_아이디,
                                          @PathVariable("issueKey") String 이슈_키,
                                          @RequestParam("page") int 페이지_번호,
                                          @RequestParam("size") int 페이지_사이즈);

    /*
     * 클라우드
     */
    @GetMapping("/{connectId}/jira/issuetype/project/{projectId}")
    public List<지라이슈유형_데이터> 클라우드_프로젝트별_이슈_유형_목록(@PathVariable("connectId") String 연결_아이디,
                                                @PathVariable("projectId") String 프로젝트_아이디);

    @GetMapping("/{connectId}/jira/issuestatus/project/{projectId}")
    public List<지라이슈상태_데이터> 클라우드_프로젝트별_이슈_상태_목록(@PathVariable("connectId") String 연결_아이디,
                                                @PathVariable("projectId") String 프로젝트_아이디);

    /*
     * 검색엔진
     */
    @GetMapping("/engine/jira/{connectId}/issue/loadToES/bulk/{issueKey}")
    public int 이슈_검색엔진_벌크_저장(
            @PathVariable("connectId") Long 지라서버_아이디,
            @PathVariable("issueKey") String 이슈_키,
            @RequestParam("pdServiceId") Long 제품서비스_아이디,
            @RequestParam("pdServiceVersion") Long 제품서비스_버전_아이디);

    @GetMapping("/engine/jira/{connectId}/issue/getProgress/{pdService}/{pdServiceVersion}")
    public Map<String, Long> 제품서비스_버전별_상태값_통계(@PathVariable("connectId") Long 지라서버_아이디,
                                              @PathVariable("pdService") Long 제품서비스_아이디,
                                              @PathVariable("pdServiceVersion") Long 제품서비스_버전_아이디);

    /*
     * 요구사항 상세 페이지
     */
    @GetMapping("/engine/jira/{connectId}/issue/getReqCount/{pdServiceId}")
    Map<String, Long> 제품서비스별_담당자_요구사항_통계(@PathVariable("connectId") Long 지라서버_아이디,
                                         @PathVariable("pdServiceId") Long 제품서비스_아이디,
                                         @RequestParam("assigneeEmail") String 담당자_이메일);

    @GetMapping("/engine/jira/{connectId}/issue/getReqCount/{pdServiceId}/{issueKey}")
    Map<String, Long> 제품서비스별_담당자_연관된_요구사항_통계(@PathVariable("connectId") Long 지라서버_아이디,
                                             @PathVariable("pdServiceId") Long 제품서비스_아이디,
                                             @PathVariable("issueKey") String 이슈키,
                                             @RequestParam("assigneeEmail") String 담당자_이메일);


    @GetMapping("/engine/jira/{connectId}/issue/pdService/pdServiceVersions")
    List<지라이슈> 제품서비스_버전목록으로_조회(@PathVariable("connectId") Long 지라서버_아이디,
                               @RequestParam Long pdServiceLink,
                               @RequestParam List<Long> pdServiceVersionLinks);

    /*
     * 이슈 가져오기.
     */
    @GetMapping("/engine/jira/{connectId}/issue/get/{reqProjectKey}/{reqIssueKey}")
    지라이슈 요구사항이슈_조회(@PathVariable("connectId") Long 지라서버_아이디,
                   @PathVariable("reqProjectKey") String 지라프로젝트_키,
                   @PathVariable("reqIssueKey") String 지라이슈_키);

    @GetMapping("/engine/jira/{connectId}/issue/pdService/pdServiceVersions/heatmap")
    히트맵데이터 히트맵_제품서비스_버전목록으로_조회(@PathVariable("connectId") Long 지라서버_아이디,
                               @RequestParam Long pdServiceLink,
                               @RequestParam List<Long> pdServiceVersionLinks);


    @PostMapping("/engine/jira/dashboard/requirement-linkedissue/{pdServiceId}")
    ResponseEntity<List<지라이슈>> 제품별_요구사항_연결이슈_조회(@PathVariable("pdServiceId") Long pdServiceId,
                                                @RequestBody 지라이슈_일반_검색_요청 지라이슈_일반_검색_요청);

}