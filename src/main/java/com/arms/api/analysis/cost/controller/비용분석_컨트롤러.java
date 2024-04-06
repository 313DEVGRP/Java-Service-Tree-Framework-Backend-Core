package com.arms.api.analysis.cost.controller;

import com.arms.api.analysis.common.AggregationMapper;
import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.cost.dto.*;
import com.arms.api.analysis.cost.service.비용서비스;
import com.arms.api.requirement.reqadd.model.ReqAddDTO;
import com.arms.api.util.API호출변수;
import com.arms.api.util.communicate.external.request.aggregation.EngineAggregationRequestDTO;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일반_집계_요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/arms/analysis/cost")
public class 비용분석_컨트롤러 {
    private final AggregationMapper aggregationMapper;

    private final 비용서비스 비용서비스;

    private final 통계엔진통신기 통계엔진통신기;

    @GetMapping("/all-assignees")
    public ResponseEntity<CommonResponse.ApiResult<버전요구사항별_담당자데이터>> 전체_담당자가져오기(AggregationRequestDTO aggregationRequestDTO) {

        long 시작시간 = System.currentTimeMillis();

        log.info(" [ " + this.getClass().getName() + " :: 전체_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        log.info(aggregationRequestDTO.toString());

        Long 제품아이디 = aggregationRequestDTO.getPdServiceLink();
        if (제품아이디 == null) {
            return null;
        }

        List<Long> 버전아이디_목록 = aggregationRequestDTO.getPdServiceVersionLinks();
        if (버전아이디_목록 == null) {
            return null;
        }

        String 하위그룹필드 = API호출변수.담당자이름집계;
        지라이슈_일반_집계_요청 일반_집계_요청_세팅 = 지라이슈_일반_집계_요청.builder()
                .메인그룹필드(API호출변수.담당자아이디집계)
                .컨텐츠보기여부(true)
                .크기(1000)
                .하위그룹필드들(Arrays.stream(하위그룹필드.split(",")).collect(Collectors.toList()))
                .build();

        버전요구사항별_담당자데이터 결과 = 비용서비스.전체_담당자가져오기(제품아이디, 버전아이디_목록, 일반_집계_요청_세팅);

        long 종료시간 = System.currentTimeMillis();

        long 걸린시간 = 종료시간 - 시작시간;
        log.info("API 호출이 걸린 시간: " + 걸린시간 + "밀리초");

        return ResponseEntity.ok(CommonResponse.success(결과));
    }

    /**
     * 버전별 요구사항별 담당자 조회 API
     */
    @GetMapping("/version-req-assignees")
    public ResponseEntity<CommonResponse.ApiResult<버전요구사항별_담당자데이터>> 버전별_요구사항별_담당자가져오기(AggregationRequestDTO aggregationRequestDTO) {
        long 시작시간 = System.currentTimeMillis();

        log.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항별_담당자가져오기 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        log.info(aggregationRequestDTO.toString());
        EngineAggregationRequestDTO engineAggregationRequestDTO = aggregationMapper.toEngineAggregationRequestDTO(aggregationRequestDTO);
        버전요구사항별_담당자데이터 결과 = 비용서비스.버전별_요구사항별_담당자가져오기(engineAggregationRequestDTO);

        long 종료시간 = System.currentTimeMillis();

        long 걸린시간 = 종료시간 - 시작시간;
        log.info("API 호출이 걸린 시간: " + 걸린시간 + "밀리초");
        return ResponseEntity.ok(CommonResponse.success(결과));
    }

    @GetMapping("/{changeReqTableName}/req-difficulty-priority-list")
    public ModelAndView 요구사항목록_난이도_및_우선순위통계_가져오기(
            @PathVariable(value = "changeReqTableName") String changeReqTableName,
            ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("[ ReqAddController :: 요구사항목록_난이도_및_우선순위통계_가져오기 ]");

        SessionUtil.setAttribute("req-difficulty-priority-list", changeReqTableName);

        요구사항목록_난이도_및_우선순위통계데이터 조회결과 = 비용서비스.요구사항목록_난이도_및_우선순위통계_가져오기(reqAddDTO);

        SessionUtil.removeAttribute("req-difficulty-priority-list");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 조회결과);
        return modelAndView;
    }

    /**
     * 버전별 요구사항별 연결된 지라 지라 이슈 아이디 조회 API (버블 차트)
     */
    @GetMapping("/req-linked-issue")
    public ModelAndView 버전별_요구사항_연결된_지라이슈키(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        log.info(" [ " + this.getClass().getName() + " :: 버전별_요구사항_연결된_지라이슈키 ] :: 지라이슈_제품_및_제품버전_검색요청 -> ");
        log.info(aggregationRequestDTO.toString());
        EngineAggregationRequestDTO engineAggregationRequestDTO = aggregationMapper.toEngineAggregationRequestDTO(aggregationRequestDTO);
        버전별_요구사항별_연결된_지라이슈데이터 검색결과 = 비용서비스.버전별_요구사항_연결된_지라이슈키(engineAggregationRequestDTO);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 검색결과);
        return modelAndView;
    }


    /*
     *  해당 요구사항 지라키로 업데이트 이력 조회 API
     * */
    @GetMapping("/req-updated-list")
    public ModelAndView 요구사항_지라이슈키별_업데이트_목록(@RequestParam List<String> issueList) throws Exception {
        log.info(" [ " + this.getClass().getName() + " :: 요구사항_지라이슈키별_업데이트_목록 ] :: 요구사항_지라이슈키_목록 -> ");
        log.info(issueList.toString());

        ResponseEntity<Map<String, List<요구사항_지라이슈키별_업데이트_목록_데이터>>> 검색결과 = 통계엔진통신기.요구사항_지라이슈키별_업데이트_목록(issueList);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 검색결과);
        return modelAndView;
    }

    /**
     * 제품에 대한 누적 월 별 비용 조회 API
     */
    @GetMapping("/product-accumulate-cost-by-month")
    public ResponseEntity<CommonResponse.ApiResult<ProductCostResponse>> 제품에대한투자비용대비성과(AggregationRequestDTO aggregationRequestDTO) throws Exception {
        EngineAggregationRequestDTO engineAggregationRequestDTO = aggregationMapper.toEngineAggregationRequestDTO(aggregationRequestDTO);
        ProductCostResponse productCostResponse = 비용서비스.calculateInvestmentPerformance(engineAggregationRequestDTO);
        return ResponseEntity.ok(CommonResponse.success(productCostResponse));
    }


}
