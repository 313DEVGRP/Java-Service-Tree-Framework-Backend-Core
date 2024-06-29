package com.arms.api.analysis.time.controller;

import com.arms.api.analysis.time.model.등고선데이터;
import com.arms.api.analysis.time.model.일자별_요구사항_연결된이슈_생성개수_및_상태데이터;
import com.arms.api.analysis.time.service.TimeService;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.api.util.communicate.external.request.aggregation.지라이슈_일자별_제품_및_제품버전_검색요청;
import com.arms.api.analysis.time.model.히트맵데이터;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/admin/arms/analysis/time")
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;

    // 히트맵
    @GetMapping(value = "/heatmap")
    public ModelAndView 히트맵_제품서비스_버전목록으로_조회(@RequestParam Long pdServiceLink,
                                            @RequestParam List<Long> pdServiceVersionLinks) throws Exception {

        log.info(" [ 일정분석_컨트롤러 :: 히트맵_제품서비스_버전목록으로_조회 ] ");
        히트맵데이터 result = timeService.히트맵_제품서비스_버전목록으로_조회(pdServiceLink, pdServiceVersionLinks);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }


    // 스캐터차트, 멀티콤비네이션 차트
    @GetMapping("/standard-daily/jira-issue")
    public ModelAndView 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) throws Exception {

        log.info("[일정분석_컨트롤러 :: 기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계] " +
                ":: 지라이슈 일자별 제품 및 제품버전 검색요청 -> " + 지라이슈_일자별_제품_및_제품버전_검색요청.toString());

        Map<String, 일자별_요구사항_연결된이슈_생성개수_및_상태데이터> result =
                timeService.기준일자별_제품_및_제품버전목록_요구사항_및_연결된이슈_집계(지라이슈_일자별_제품_및_제품버전_검색요청);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @GetMapping("/standard-daily/updated-jira-issue")
    public ModelAndView 기준일자별_제품_및_제품버전목록_업데이트된_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) throws Exception {

        log.info("[일정분석_컨트롤러 :: 기준일자별_제품_및_제품버전목록_업데이트된_이슈조회] " +
                ":: 지라이슈 일자별 제품 및 제품버전 검색요청 -> " + 지라이슈_일자별_제품_및_제품버전_검색요청.toString());


        Map<Long, List<지라이슈>> 버전별_그룹화_결과
                = timeService.기준일자별_제품_및_제품버전목록_업데이트된_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 버전별_그룹화_결과);

        return modelAndView;

    }

    @GetMapping("/standard-daily/updated-ridgeline")
    public List<등고선데이터> 기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청 지라이슈_일자별_제품_및_제품버전_검색요청) throws Exception {

        log.info("[일정분석_컨트롤러 :: 기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회] " +
                ":: 지라이슈 일자별 제품 및 제품버전 검색요청 -> " + 지라이슈_일자별_제품_및_제품버전_검색요청.toString());

        List<등고선데이터>  결과 = timeService.기준일자별_제품_및_제품버전목록_업데이트된_누적_이슈조회(지라이슈_일자별_제품_및_제품버전_검색요청);

        return 결과;
    }

}
