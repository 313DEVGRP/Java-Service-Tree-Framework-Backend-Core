package com.arms.analysis.scope.controller;

import com.arms.analysis.scope.dto.TreeBarDTO;
import com.arms.analysis.scope.service.ScopeService;
import com.arms.util.external_communicate.dto.*;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.arms.util.external_communicate.통계엔진통신기;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;

import java.util.Arrays;
import java.util.List;
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

    static final long dummy_jira_server = 0L;

    @GetMapping("/getReqPerVersion/{pdServiceId}")
    public ResponseEntity<검색결과_목록_메인> 버전들_하위_요구사항_연결이슈_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                                     @RequestParam List<Long> pdServiceVersionLinks,
                                                     지라이슈_단순_집계_요청 검색요청_데이터) {
        log.info("스코프분석_컨트롤러 :: 버전들_하위_요구사항_연결이슈_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceId.toString(), pdServiceVersionLinks.toString());
        ResponseEntity<검색결과_목록_메인> 집계결과 = 통계엔진통신기.일반_버전필터_검색(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        return ResponseEntity.ok(집계결과.getBody());

    }

    @GetMapping("/req-status-and-reqInvolved-unique-assignees")
    public  ResponseEntity<List<제품_서비스_버전>> 요구사항_별_상태_및_관여_작업자_수(
            지라이슈_제품_및_제품버전_병합_검색_요청 지라이슈_제품_및_제품버전_병합_검색_요청) {
        log.info("[ 스코프분석_컨트롤러 :: 요구사항_별_상태_및_관여_작업자_수2 ] :: 요구_사항, 하위_이슈_사항");
        log.info(지라이슈_제품_및_제품버전_병합_검색_요청.get요구_사항().getPdServiceLink().toString());
        log.info(지라이슈_제품_및_제품버전_병합_검색_요청.get요구_사항().toString());
        log.info(지라이슈_제품_및_제품버전_병합_검색_요청.get하위_이슈_사항().toString());
        ResponseEntity<List<제품_서비스_버전>> 통신결과 = 통계엔진통신기.요구사항_별_상태_및_관여_작업자_수(지라이슈_제품_및_제품버전_병합_검색_요청);

        return ResponseEntity.ok(통신결과.getBody());
    }

    @GetMapping("/req-status-and-reqInvolved-unique-assignees2")
    public  ResponseEntity<List<제품_서비스_버전>> 요구사항_별_상태_및_관여_작업자_수2(
            지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) {

        log.info("[ 스코프분석_컨트롤러 :: 요구사항_별_상태_및_관여_작업자_수2 ] :: 지라이슈_제품_및_제품버전_검색요청.pdServiceLink ==> {}, pdServiceVersionLinks ==> {}",
                지라이슈_제품_및_제품버전_검색요청.getPdServiceLink(), 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks().toString());

        ResponseEntity<List<제품_서비스_버전>> 통신결과 = 통계엔진통신기.요구사항_별_상태_및_관여_작업자_수3(지라이슈_제품_및_제품버전_검색요청);

        String 하위그룹필드 = "key,status.status_name.keyword";
        지라이슈_일반_집계_요청 일반_검색 = 지라이슈_일반_집계_요청.builder()
                .isReq(true)
                .메인그룹필드("pdServiceVersion")
                .컨텐츠보기여부(true)
                .크기(1000)
                .하위그룹필드들(Arrays.stream(하위그룹필드.split(",")).collect(Collectors.toList()))
                .build();
        ResponseEntity<검색결과_목록_메인> 검색결과 = 통계엔진통신기.제품서비스_일반_버전_통계(지라이슈_제품_및_제품버전_검색요청.getPdServiceLink(), 지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks(), 일반_검색);

        List<제품_서비스_버전> 매핑결과 = scopeService.요구사항_상태_매핑(통신결과.getBody(), 검색결과.getBody().get검색결과());

        return ResponseEntity.ok(매핑결과);
    }

    @GetMapping("/tree-bar-top10")
    public ResponseEntity<CommonResponse.ApiResult<List<TreeBarDTO>>> treeBar(
            지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청
    ) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(scopeService.treeBar(지라이슈_제품_및_제품버전_검색요청)));
    }


}
