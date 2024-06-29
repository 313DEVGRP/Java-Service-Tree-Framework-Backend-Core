package com.arms.api.analysis.scope.controller;

import com.arms.api.analysis.scope.dto.TreeBarDTO;
import com.arms.api.analysis.scope.dto.버전별_요구사항_상태_작업자수;
import com.arms.api.analysis.scope.dto.요구사항_버전명추가_DTO;
import com.arms.api.analysis.scope.service.ScopeService;
import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.util.communicate.external.AggregationService;
import com.arms.api.util.communicate.external.request.aggregation.요구사항_버전_이슈_키_상태_작업자수;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/admin/arms/analysis/scope")
@RequiredArgsConstructor
public class ScopeController {

    private final AggregationService engineCommunicator;

    private final ScopeService scopeService;

    @GetMapping(value = "/pdService/pdServiceVersions")
    public ModelAndView 제품서비스_버전목록으로_조회(@RequestParam Long pdServiceLink,
                                        @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info(" [ 일정분석_컨트롤러 :: 제품서비스_버전목록으로_조회 ] ");
        List<지라이슈> result = scopeService.제품서비스_버전목록으로_조회(pdServiceLink, pdServiceVersionLinks);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;

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

    @GetMapping("/state-per-version/{changeReqTableName}/getReqAddListByFilter.do")
    public ResponseEntity<List<요구사항_버전명추가_DTO>> 버전별_요구사항_상태정보(@PathVariable(value ="changeReqTableName") String changeReqTableName
            , @RequestParam Long pdServiceId
            , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", "");
        log.info("스코프분석_컨트롤러 :: 버전별_요구사항_상태정보.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceStr, pdServiceVersionLinks);

        return  ResponseEntity.ok(scopeService.버전_요구사항_상태(changeReqTableName, pdServiceId, pdServiceVersionLinks));

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
    public ResponseEntity<CommonResponse.ApiResult<List<TreeBarDTO>>> treeBar(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(scopeService.treeBar(aggregationRequestDTO)));
    }
}
