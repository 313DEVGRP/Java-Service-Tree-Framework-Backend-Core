package com.arms.analysis.time.controller;

import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈;
import com.arms.util.external_communicate.dto.지라이슈_일반_검색_요청;
import com.arms.util.external_communicate.dto.히트맵데이터;
import com.arms.util.external_communicate.엔진통신기;
import com.arms.util.external_communicate.통계엔진통신기;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = "/arms/analysis/time")
public class 일정분석_컨트롤러 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 엔진통신기 엔진통신기;

    @Autowired
    private 통계엔진통신기 통계엔진통신기;

    static final long dummy_jira_server = 0L;

    @GetMapping(value = "/pdService/pdServiceVersions")
    @ResponseBody
    public ModelAndView 제품서비스_버전목록으로_조회(@RequestParam Long pdServiceLink,
                                        @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품서비스_버전목록으로_조회");
        List<지라이슈> result = 엔진통신기.제품서비스_버전목록으로_조회(dummy_jira_server, pdServiceLink, pdServiceVersionLinks);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @GetMapping(value = "/heatmap")
    @ResponseBody
    public ModelAndView 히트맵_제품서비스_버전목록으로_조회(@RequestParam Long pdServiceLink,
                                            @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info("일정분석_컨트롤러 :: 히트맵_제품서비스_버전목록으로_조회");
        히트맵데이터 result = 엔진통신기.히트맵_제품서비스_버전목록으로_조회(dummy_jira_server, pdServiceLink, pdServiceVersionLinks);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/normal-version/{pdServiceId}")
    public ModelAndView 제품서비스_일반_버전_통계(@PathVariable("pdServiceId") Long pdServiceId,
                                       @RequestParam List<Long> pdServiceVersionLinks,
                                       지라이슈_일반_검색_요청 검색요청_데이터) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품서비스_일반_버전_집계 pdServiceId ==> {}, pdServiceVersionLinks ==> {}", pdServiceId, pdServiceVersionLinks.toString());

        ResponseEntity<검색결과_목록_메인> 요구사항_연결이슈_일반_버전_통계
                = 통계엔진통신기.제품서비스_일반_버전_통계(pdServiceId, pdServiceVersionLinks, 검색요청_데이터);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        검색결과_목록_메인 통계결과 = 요구사항_연결이슈_일반_버전_통계.getBody();
        modelAndView.addObject("result", 통계결과);
        return modelAndView;
    }
}
