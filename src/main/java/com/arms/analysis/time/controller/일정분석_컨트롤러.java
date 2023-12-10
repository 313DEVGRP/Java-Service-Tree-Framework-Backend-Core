package com.arms.analysis.time.controller;

import com.arms.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.dashboard.model.combination.RequirementJiraIssueAggregationResponse;
import com.arms.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.util.external_communicate.dto.search.검색결과_목록_메인;
import com.arms.util.external_communicate.dto.지라이슈;
import com.arms.util.external_communicate.dto.지라이슈_일반_검색_요청;
import com.arms.util.external_communicate.dto.지라이슈_제품_및_제품버전_검색요청;
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

    @ResponseBody
    @GetMapping("/daily-requirements-jira-issue-statuses")
    public ModelAndView 제품_혹은_제품버전들의_요구사항_지라이슈상태_일별_집계(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품_혹은_제품버전들의_요구사항_지라이슈상태_일별_집계");

        Map<Long, String> versionStartDates = pdServiceVersion.getVersionStartDates(지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks());
        log.info(versionStartDates.toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate minDate = null;

        for (String value : versionStartDates.values()) {
            try {
                LocalDate date = LocalDate.parse(value.split(" ")[0], formatter);
                //System.out.println("formatting date: " + date);
                if (minDate == null || date.isBefore(minDate)) {
                    minDate = date;
                }
            } catch (DateTimeParseException e) {
                // value가 날짜 형식이 아닌 경우
            }
        }

        if (minDate == null) {
            LocalDate currentDate = LocalDate.now();
            LocalDate sevenDaysAgo = currentDate.minusDays(7);
            minDate = sevenDaysAgo; // 일주일 전 날짜로 설정
        }

        log.info("start date: " + String.valueOf(minDate));
        Map<String, RequirementJiraIssueAggregationResponse> result = 통계엔진통신기.제품_혹은_제품버전들의_요구사항_지라이슈상태_일별_집계(지라이슈_제품_및_제품버전_검색요청, String.valueOf(minDate)).getBody();
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/daily-requirements-count/jira-issue-statuses")
    public ModelAndView 제품_혹은_제품버전들의_이슈생성개수_및_상태_일별_집계(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청,
                                                       @RequestParam(required = false) String startDate) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품_혹은_제품버전들의_이슈생성개수_및_상태_일별_집계");

        if (startDate == null || startDate.isEmpty()) {
            Map<Long, String> versionStartDates = pdServiceVersion.getVersionStartDates(지라이슈_제품_및_제품버전_검색요청.getPdServiceVersionLinks());
            log.info(versionStartDates.toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate minDate = null;

            for (String value : versionStartDates.values()) {
                try {
                    LocalDate date = LocalDate.parse(value.split(" ")[0], formatter);
                    if (minDate == null || date.isBefore(minDate)) {
                        minDate = date;
                    }
                } catch (DateTimeParseException e) {
                    // value가 날짜 형식이 아닌 경우
                }
            }

            if (minDate == null) {
                LocalDate currentDate = LocalDate.now();
                LocalDate sevenDaysAgo = currentDate.minusDays(7);
                minDate = sevenDaysAgo; // 일주일 전 날짜로 설정
            }

            startDate = String.valueOf(minDate);
        }

        Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터> result = 통계엔진통신기.제품_혹은_제품버전들의_이슈생성개수_및_상태_일별_집계(지라이슈_제품_및_제품버전_검색요청, String.valueOf(startDate)).getBody();
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @GetMapping(value = "/weekly-updated-issue-search")
    @ResponseBody
    public ModelAndView 제품서비스_버전목록으로_주간_업데이트된_이슈조회(지라이슈_제품_및_제품버전_검색요청 지라이슈_제품_및_제품버전_검색요청, @RequestParam Integer baseWeek) throws Exception {

        log.info("일정분석_컨트롤러 :: 제품서비스_버전목록으로_주간_업데이트된_이슈조회");
        List<지라이슈> 오늘기준_일주일_데이터 = 통계엔진통신기.제품서비스_버전목록으로_주간_업데이트된_이슈조회(지라이슈_제품_및_제품버전_검색요청 ,baseWeek);


        Map<Long, List<지라이슈>> 버전별_그룹화_결과 = 오늘기준_일주일_데이터.stream()
                .collect(Collectors.groupingBy(지라이슈::getPdServiceVersion));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 버전별_그룹화_결과);

        return modelAndView;
    }
}
