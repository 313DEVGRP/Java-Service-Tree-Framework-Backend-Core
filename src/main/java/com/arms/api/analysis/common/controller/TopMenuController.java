package com.arms.api.analysis.common.controller;

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.common.service.TopMenuService;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/arms/analysis/top-menu")
@RequiredArgsConstructor
public class TopMenuController {

    private final TopMenuService topMenuService;

    // TopMenuApi용
    @GetMapping("/{changeReqTableName}/getReqAddListByFilter.do")
    public ResponseEntity<Map<String, Long>> 분석_톱메뉴_요구사항_상태_합계(@PathVariable(value ="changeReqTableName") String changeReqTableName
            , @RequestParam Long pdServiceId
            , @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", "");
        log.info("TopMenuController :: 분석_톱메뉴_요구사항_상태_합계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}"
                , pdServiceStr, pdServiceVersionLinks);

        return  ResponseEntity.ok(topMenuService.톱메뉴_버전별_요구사항_상태_합계(changeReqTableName, pdServiceId, pdServiceVersionLinks));
    }

    @GetMapping("/issue/reqAndSubtask/{pdServiceId}")
    public ResponseEntity<Map<String, Long>> 분석_톱메뉴_이슈_집계(@PathVariable("pdServiceId") Long pdServiceId
            ,@RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("TopMenuController :: 분석_톱메뉴_이슈_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks);

        return  ResponseEntity.ok(topMenuService.톱메뉴_요구사항_하위이슈_집계(pdServiceId, pdServiceVersionLinks));
    }

    @GetMapping("/resourceInfo/{pdServiceId}")
    public ResponseEntity<?> 톱메뉴_작업자별_요구사항_하위이슈_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                   @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("TopMenuController :: 리소스_작업자_통계.제품서비스의 c_id ==> {}, 선택된버전의 c_id ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        return  ResponseEntity.ok(topMenuService.톱메뉴_작업자별_요구사항_하위이슈_집계(pdServiceId, pdServiceVersionLinks));
    }
    
    @GetMapping("/normal-version/resolution") //추가 리팩토링 필요
    public ModelAndView 제품서비스_일반_버전_해결책유무_집계(AggregationRequestDTO aggregationRequestDTO,
                                             @RequestParam(required = false) String resolution) {
        log.info(" [ TopMenuController :: 제품서비스_일반_버전_해결책유무_집계 ] " +
                        "pdServiceId ==> {}, pdServiceVersionLinks ==> {}, resolution ==> {}"
                , aggregationRequestDTO.getPdServiceLink()
                , aggregationRequestDTO.getPdServiceVersionLinks().toString()
                , resolution);

        검색결과_목록_메인 요구사항_연결이슈_일반_버전_해결책통계
                = topMenuService.제품서비스_일반_버전_해결책유무_통계(aggregationRequestDTO, resolution);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 요구사항_연결이슈_일반_버전_해결책통계);
        return modelAndView;
    }
}
