package com.arms.api.analysis.scope.controller;

import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.analysis.scope.service.ScopeService;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.service.ReqAdd;
import com.arms.api.util.external_communicate.dto.*;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.arms.api.util.external_communicate.통계엔진통신기;
import com.arms.api.util.external_communicate.dto.search.검색결과_목록_메인;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/arms/analysis/scope")
public class 스코프분석_컨트롤러 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private ReqAdd reqAdd;

    static final long dummy_jira_server = 0L;

    @GetMapping("/pdservice-id/{pdServiceId}/req-per-version")
    public ResponseEntity<검색결과_목록_메인> 버전들_하위_요구사항_연결이슈_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                                                        @RequestParam List<Long> pdServiceVersionLinks) {
        log.info("스코프분석_컨트롤러 :: 버전들_하위_요구사항_연결이슈_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceId, pdServiceVersionLinks);
        지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
                .메인그룹필드("pdServiceVersions")
                .하위그룹필드들(List.of("isReq"))
                .컨텐츠보기여부(false)
                .build();

        ResponseEntity<검색결과_목록_메인> 집계결과 = 통계엔진통신기.일반_버전필터_검색(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);
        return ResponseEntity.ok(집계결과.getBody());

    }

    @GetMapping("/req-per-version/{changeReqTableName}/getReqAddListByFilter.do")
    public ResponseEntity<검색결과_목록_메인> 버전들_하위_요구사항(@PathVariable(value ="changeReqTableName") String changeReqTableName
                                                                , @RequestParam Long pdServiceId
                                                                , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", "");
        log.info("스코프분석_컨트롤러 :: 버전들_하위_요구사항.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceStr, pdServiceVersionLinks);

        scopeService.버전_요구사항_자료(changeReqTableName, pdServiceId, pdServiceVersionLinks);

        return null;

    }

    @PostMapping("/req-status-and-reqInvolved-unique-assignees")
    public ResponseEntity<List<제품_서비스_버전>> 요구사항_별_상태_및_관여_작업자_수(
           @RequestBody 지라이슈_제품_및_제품버전_병합_집계_요청 지라이슈_제품_및_제품버전_병합_집계_요청) {
        Long 선택된_제품서비스_아이디 = 지라이슈_제품_및_제품버전_병합_집계_요청.get요구_사항().getPdServiceLink();
        List<Long> 선택된_버전_목록 = 지라이슈_제품_및_제품버전_병합_집계_요청.get요구_사항().getPdServiceVersionLinks();
        log.info("[ 스코프분석_컨트롤러 :: 요구사항_별_상태_및_관여_작업자_수 ] 백엔드 요청");
        log.info("요구_사항 :: 선택된_제품_서비스 아이디 : {}", 선택된_제품서비스_아이디);
        log.info("요구_사항 :: 선택된_제품_서비스 버전 : {}", 선택된_버전_목록);

        ResponseEntity<List<제품_서비스_버전>> 요구사항_별_상태_및_관여_작업자_수_통신결과 = 통계엔진통신기.요구사항_별_상태_및_관여_작업자_수(지라이슈_제품_및_제품버전_병합_집계_요청);

        String 하위그룹필드 = "key,status.status_name.keyword";
        지라이슈_일반_집계_요청 일반_집계_요청_세팅 = 지라이슈_일반_집계_요청.builder()
                .isReq(true)
                .메인그룹필드("pdServiceVersions")
                .컨텐츠보기여부(true)
                .크기(1000)
                .하위그룹필드들(Arrays.stream(하위그룹필드.split(",")).collect(Collectors.toList()))
                .build();

        ResponseEntity<검색결과_목록_메인> 제품서비스_일반_버전_통계_통신결과 =
                통계엔진통신기.제품서비스_일반_버전_통계(선택된_제품서비스_아이디, 선택된_버전_목록, 일반_집계_요청_세팅);

        List<제품_서비스_버전> 매핑결과 = Optional.ofNullable(요구사항_별_상태_및_관여_작업자_수_통신결과)
                .map(ResponseEntity::getBody)
                .flatMap(요구사항_별_상태_및_관여_작업자_수 ->
                        Optional.ofNullable(제품서비스_일반_버전_통계_통신결과)
                                .map(ResponseEntity::getBody)
                                .map(검색결과_목록_메인::get검색결과)
                                .map(검색결과 -> scopeService.요구사항_상태_매핑(요구사항_별_상태_및_관여_작업자_수, 검색결과))
                )
                .orElseGet(() -> {
                    log.info("[ 스코프분석_컨트롤러 :: 요구사항_별_상태_및_관여_작업자_수 ] 매핑결과가 null 입니다.");
                    return null;
                });
        return ResponseEntity.ok(매핑결과);
    }

    @GetMapping("/tree-bar-top10")
    public ResponseEntity<CommonResponse.ApiResult<List<TreeBarDTO>>> treeBar(
            지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    ) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(scopeService.treeBar(지라이슈_제품_및_제품버전_검색요청)));
    }


}
