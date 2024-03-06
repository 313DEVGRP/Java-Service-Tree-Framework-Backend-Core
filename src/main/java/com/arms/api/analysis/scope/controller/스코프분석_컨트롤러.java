package com.arms.api.analysis.scope.controller;

import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.analysis.scope.dto.버전별_요구사항_상태_작업자수;
import com.arms.api.analysis.scope.service.ScopeService;
import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.common.AggregationMapper;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.request.aggregation.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.external_communicate.dto.*;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/arms/analysis/scope")
@RequiredArgsConstructor
public class 스코프분석_컨트롤러 {

    private final 통계엔진통신기 통계엔진통신기;

    private final ScopeService scopeService;

    private final AggregationMapper aggregationMapper;

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
    public ResponseEntity<Map<String, Long>> 버전들_하위_요구사항(@PathVariable(value ="changeReqTableName") String changeReqTableName
                                                                , @RequestParam Long pdServiceId
                                                                , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", "");
        log.info("스코프분석_컨트롤러 :: 버전들_하위_요구사항.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceStr, pdServiceVersionLinks);

        Map<String, Long> 버전_요구사항_수 = scopeService.버전_요구사항_자료(changeReqTableName, pdServiceId, pdServiceVersionLinks);

        return  ResponseEntity.ok(버전_요구사항_수);
    }

    @GetMapping("/{pdServiceId}/req-status-and-reqInvolved-unique-assignees-per-version")
    public ResponseEntity<List<버전별_요구사항_상태_작업자수>> 버전배열_요구사항_별_상태_및_관여_작업자_수(@PathVariable("pdServiceId") Long pdServiceId,
                                                                            @RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("스코프분석_컨트롤러 :: 버전배열_요구사항_별_상태_및_관여_작업자_수.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceId, pdServiceVersionLinks);
        Map<String, List<요구사항_버전_이슈_키_상태_작업자수>> 버전묶음_요구사항_목록 = scopeService.버전이름_매핑하고_같은_버전_묶음끼리_배치(pdServiceId, pdServiceVersionLinks);
        List<버전별_요구사항_상태_작업자수> 매핑결과 = new ArrayList<>();
        for(Map.Entry<String, List<요구사항_버전_이슈_키_상태_작업자수>> entry : 버전묶음_요구사항_목록.entrySet()) {
            버전별_요구사항_상태_작업자수  버전별_요구사항_상태_작업자수 = new 버전별_요구사항_상태_작업자수(entry.getKey(), entry.getValue());
            매핑결과.add(버전별_요구사항_상태_작업자수);
        }
        return ResponseEntity.ok(매핑결과);
    }

    @GetMapping("/tree-bar-top10")
    public ResponseEntity<CommonResponse.ApiResult<List<TreeBarDTO>>> treeBar(
            @Validated AggregationRequestDTO aggregationRequestDTO
    ) throws Exception {
        EngineAggregationRequestDTO engineAggregationRequestDTO = aggregationMapper.toEngineAggregationRequestDTO(aggregationRequestDTO);
        return ResponseEntity.ok(CommonResponse.success(scopeService.treeBar(engineAggregationRequestDTO)));
    }


}
