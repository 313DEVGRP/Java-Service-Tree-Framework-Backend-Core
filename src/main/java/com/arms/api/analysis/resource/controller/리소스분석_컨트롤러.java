package com.arms.api.analysis.resource.controller;

import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.aggregation.검색결과_목록_메인;
import com.arms.api.util.external_communicate.dto.지라이슈_일반_집계_요청;
import com.arms.api.util.external_communicate.dto.지라이슈_단순_집계_요청;
import com.arms.api.util.communicate.external.통계엔진통신기;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/arms/analysis/resource")
@RequiredArgsConstructor
public class 리소스분석_컨트롤러 {

    private final 통계엔진통신기 통계엔진통신기;
    @GetMapping("/normal-version/{pdServiceId}")
    public ModelAndView 제품서비스_일반_버전_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                       @RequestParam List<Long> pdServiceVersionLinks,
                                       지라이슈_일반_집계_요청 검색요청_데이터) {

        log.info("리소스분석_컨트롤러 :: 제품서비스_버전_집계.pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.제품서비스_일반_버전_통계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    @GetMapping("/workerStatus/{pdServiceId}")
    public ModelAndView 리소스_작업자_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                        @RequestParam List<Long> pdServiceVersionLinks,
                                        지라이슈_단순_집계_요청 검색요청_데이터) throws Exception {

        log.info("리소스분석_컨트롤러 :: 리소스_작업자_통계.제품서비스의 c_id ==> {}, 선택된버전의 c_id ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.일반_버전필터_집계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();
        modelAndView.addObject("result", 통신결과);
        return modelAndView;
    }

    @GetMapping("/normal-versionAndMail-filter/{pdServiceId}")
    public ModelAndView 리소스_버전필터_작업자필터_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                       @RequestParam List<Long> pdServiceVersionLinks,
                                       @RequestParam List<String> mailAddressList,
                                       지라이슈_단순_집계_요청 검색요청_데이터) throws Exception {

        log.info("리소스분석_컨트롤러 :: 리소스_버전필터_작업자필터_통계.pdServiceVersionLinks ==> {}, mailAddressList ==> {}"
                , pdServiceVersionLinks.toString(), mailAddressList.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_통계
                = 통계엔진통신기.일반_버전_및_작업자_필터_검색(pdServiceId, pdServiceVersionLinks, mailAddressList, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");

        검색결과_목록_메인 통신결과 = 요구사항_연결이슈_일반_통계.getBody();

        modelAndView.addObject("result", 통신결과);

        return modelAndView;
    }

    @GetMapping("/reqInAction/{pdServiceId}")
    public ModelAndView 서브태스크_부모_요구사항_집계(@PathVariable("pdServiceId") Long pdServiceId,
                                         @RequestParam List<Long> pdServiceVersionLinks,
                                         지라이슈_일반_집계_요청 검색요청_데이터) throws Exception {
        log.info("리소스분석_컨트롤러 :: 서브태스크_부모_요구사항_집계");

        ResponseEntity<검색결과_목록_메인> 집계결과 = 통계엔진통신기.제품서비스_일반_버전_통계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        검색결과_목록_메인 결과 = 집계결과.getBody();

        Map<String, Integer> result = new HashMap<>();

        Integer 부모_요구사항_목록_사이즈 = Optional.ofNullable(결과)
                .map(검색결과_목록_메인::get검색결과)
                .map(검색결과Map -> 검색결과Map.get("group_by_parentReqKey"))
                .map(검색결과_목록 -> {
                    List<String> 부모_요구사항_목록 = 검색결과_목록.stream()
                            .map(검색결과::get필드명)
                            .distinct()
                            .collect(Collectors.toList());
                    return 부모_요구사항_목록.size() - 1;
                })
                .orElse(0);

        result.put("parentReqCount", 부모_요구사항_목록_사이즈);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

}
