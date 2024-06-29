package com.arms.api.analysis.resource.controller;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.analysis.common.service.CommonService;
import com.arms.api.analysis.resource.service.ResourceService;
import com.arms.api.dashboard.model.Worker;
import com.arms.api.analysis.common.model.AggregationConstant;
import com.arms.api.util.communicate.external.AggregationService;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일반_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/admin/arms/analysis/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final AggregationService AggregationService;
    private final ResourceService resourceService;
    private final CommonService commonService;

    @GetMapping("/normal-version/{pdServiceId}")
    public ModelAndView 제품서비스_일반_버전_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                       @RequestParam List<Long> pdServiceVersionLinks,
                                       지라이슈_일반_집계_요청 검색요청_데이터) {

        log.info("리소스분석_컨트롤러 :: 제품서비스_버전_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = AggregationService.제품서비스_일반_버전_통계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    // 확인
    @GetMapping("/normal-versionAndMail-filter/{pdServiceId}")
    public ModelAndView 리소스_버전필터_작업자필터_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                          @RequestParam List<Long> pdServiceVersionLinks,
                                          @RequestParam List<String> mailAddressList,
                                          지라이슈_단순_집계_요청 검색요청_데이터) throws Exception {

        log.info("리소스분석_컨트롤러 :: 리소스_버전필터_작업자필터_통계.pdServiceVersionLinks ==> {}, mailAddressList ==> {}"
                , pdServiceVersionLinks.toString(), mailAddressList.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = AggregationService.일반_버전_및_작업자_필터_검색(pdServiceId, pdServiceVersionLinks, mailAddressList, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");

        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();

        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }

    // fetchResourceData 에서만 사용. - 확인
    @GetMapping("/workerStatus/pdServiceId/{pdServiceId}")
    public ModelAndView 작업자별_업무_처리_현황(@PathVariable("pdServiceId") Long pdServiceId,
                                      @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("[리소스분석_컨트롤러 :: 작업자별_업무_처리_현황] :: pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드(AggregationConstant.담당자_이메일_집계)
                .하위_그룹_필드들(Arrays.asList("isReq,status.status_name.keyword".split(",")))
                .컨텐츠_보기_여부(false)
                .크기(1000)
                .하위_크기(1000)
                .build();

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = AggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    // 확인
    @GetMapping("/req-subtask-pie/pdServiceId/{pdServiceId}")
    public ModelAndView 작업자_요구사항_연결이슈_파이차트(@PathVariable("pdServiceId") Long pdServiceId,
                                           @RequestParam List<Long> pdServiceVersionLinks,
                                           @RequestParam int size) throws Exception {

        log.info("[리소스분석_컨트롤러 :: 작업자별_업무_처리_현황] :: pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드("isReq")
                .하위_그룹_필드들(Arrays.asList(AggregationConstant.담당자_이메일_집계.split(",")))
                .컨텐츠_보기_여부(false)
                .크기(10)
                .하위_크기(size)
                .build();

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = AggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    // getAssigneeInfo 에서만 사용.
    @GetMapping("/assignee-infos/{pdServiceId}")
    public ModelAndView 작업자_정보_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                  @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("리소스분석_컨트롤러 :: 작업자_정보_통계.제품서비스의 c_id ==> {}, 선택된버전의 c_id ==> {}", pdServiceId, pdServiceVersionLinks.toString());
        지라이슈_단순_집계_요청 검색요청_데이터 = 지라이슈_단순_집계_요청.builder()
                .메인_그룹_필드(AggregationConstant.담당자_이메일_집계)
                .컨텐츠_보기_여부(false)
                .크기(5)
                .build();

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = AggregationService.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    // word-cloud
    @GetMapping("/aggregation/flat")
    public ResponseEntity<CommonResponse.ApiResult<검색결과_목록_메인>> commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(commonService.commonFlatAggregation(aggregationRequestDTO)));
    }

    // 리소스 트리맵
    @GetMapping("/assignees-requirements-involvements")
    ResponseEntity<CommonResponse.ApiResult<List<Worker>>> 작업자별_요구사항_관여도(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(resourceService.작업자별_요구사항_관여도(aggregationRequestDTO)));
    }
}