package com.arms.api.analysis.common.controller;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.analysis.common.service.CommonService;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/arms/analysis")
@RequiredArgsConstructor
@Slf4j
public class CommonController {

    private final CommonService commonService;


    @GetMapping("/common/aggregation/nested")
    public ResponseEntity<CommonResponse.ApiResult<검색결과_목록_메인>> commonNestedAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(commonService.commonNestedAggregation(aggregationRequestDTO)));
    }

    @GetMapping("/common/aggregation/flat")
    public ResponseEntity<CommonResponse.ApiResult<검색결과_목록_메인>> commonFlatAggregation(AggregationRequestDTO aggregationRequestDTO) {
        return ResponseEntity.ok(CommonResponse.success(commonService.commonFlatAggregation(aggregationRequestDTO)));
    }

    // TopMenuApi용
    @GetMapping("/top-menu/{changeReqTableName}/getReqAddListByFilter.do")
    public ResponseEntity<Map<String, Long>> 분석_톱메뉴_요구사항_상태_합계(@PathVariable(value = "changeReqTableName") String changeReqTableName
            , @RequestParam Long pdServiceId
            , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", "");
        log.info("TopMenuController :: 분석_톱메뉴_요구사항_상태_합계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceStr, pdServiceVersionLinks);

        return ResponseEntity.ok(commonService.톱메뉴_버전별_요구사항_상태_합계(changeReqTableName, pdServiceId, pdServiceVersionLinks));
    }

    @GetMapping("/top-menu/issue/reqAndSubtask/{pdServiceId}")
    public ResponseEntity<Map<String, Long>> 분석_톱메뉴_이슈_집계(@PathVariable("pdServiceId") Long pdServiceId
            , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("TopMenuController :: 분석_톱메뉴_이슈_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks);

        return ResponseEntity.ok(commonService.톱메뉴_요구사항_하위이슈_집계(pdServiceId, pdServiceVersionLinks));
    }

    @GetMapping("/top-menu/resourceInfo/{pdServiceId}")
    public ResponseEntity<?> 톱메뉴_작업자별_요구사항_하위이슈_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                                   @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("TopMenuController :: 리소스_작업자_통계.제품서비스의 c_id ==> {}, 선택된버전의 c_id ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        return ResponseEntity.ok(commonService.톱메뉴_작업자별_요구사항_하위이슈_집계(pdServiceId, pdServiceVersionLinks));
    }

    @GetMapping("/top-menu/normal-version/resolution") //추가 리팩토링 필요
    public ModelAndView 제품서비스_일반_버전_해결책유무_집계(AggregationRequestDTO aggregationRequestDTO,
                                             @RequestParam(required = false) String resolution) {
        log.info(" [ TopMenuController :: 제품서비스_일반_버전_해결책유무_집계 ] " +
                        "pdServiceId ==> {}, pdServiceVersionLinks ==> {}, resolution ==> {}"
                , aggregationRequestDTO.getPdServiceLink()
                , aggregationRequestDTO.getPdServiceVersionLinks().toString()
                , resolution);

        검색결과_목록_메인 요구사항_연결이슈_일반_버전_해결책통계
                = commonService.제품서비스_일반_버전_해결책유무_통계(aggregationRequestDTO, resolution);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 요구사항_연결이슈_일반_버전_해결책통계);
        return modelAndView;
    }
}
