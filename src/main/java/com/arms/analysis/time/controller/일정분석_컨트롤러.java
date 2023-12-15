package com.arms.analysis.time.controller;

import com.arms.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.util.external_communicate.dto.*;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private PdServiceVersion pdServiceVersion;

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

    @GetMapping(value = "/weekly-updated-issue-search")
    @ResponseBody
    public ModelAndView 제품서비스_버전목록으로_주간_업데이트된_이슈조회(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청,
                                                   @RequestParam Integer baseWeek,
                                                   @RequestParam String sortField) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품서비스_버전목록으로_주간_업데이트된_이슈조회");
        List<지라이슈> 오늘기준_일주일_데이터 = 통계엔진통신기.제품서비스_버전목록으로_주간_업데이트된_이슈조회(지라이슈_제품_및_제품버전_검색요청, baseWeek, sortField);


        Map<Long, List<지라이슈>> 버전별_그룹화_결과 = 오늘기준_일주일_데이터.stream()
                .collect(Collectors.groupingBy(지라이슈::getPdServiceVersion));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 버전별_그룹화_결과);

        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/standard-daily/jira-issue")
    public ModelAndView 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) throws Exception {

        log.info("[일정분석_컨트롤러 :: 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계] :: 지라이슈 일자별 제품 및 제품버전 검색요청 -> " + 지라이슈_일자별_제품_및_제품버전_검색요청.toString());

        Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터> result = 통계엔진통신기.기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청).getBody();
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }
}
