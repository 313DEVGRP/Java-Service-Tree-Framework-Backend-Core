package com.arms.analysis.time.controller;

import com.arms.util.external_communicate.dto.지라이슈;
import com.arms.util.external_communicate.엔진통신기;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
}
