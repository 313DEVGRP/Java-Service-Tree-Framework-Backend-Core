package com.arms.dashboard.controller;

import com.arms.dashboard.model.AggregationResponse;
import com.arms.globaltreemap.controller.TreeMapAbstractController;
import com.arms.jira.jiraserver.service.JiraServer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/arms/dashboard")
public class DashboardController extends TreeMapAbstractController {

    @Autowired
    private com.arms.util.external_communicate.엔진통신기 엔진통신기;

    @Autowired
    private com.arms.util.external_communicate.통계엔진통신기 통계엔진통신기;

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final long dummy_jira_server = 0L;

    @RequestMapping(value = "/getVersionProgress", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getVersionProgress(HttpServletRequest request) throws Exception {
        /* 임시 틀 생성 */
        String 제품서비스_아이디 = request.getParameter("pdserviceId");
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result",
                엔진통신기.제품서비스_버전별_상태값_통계(dummy_jira_server, 11L, 10L));

        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/jira-issue-statuses")
    public ModelAndView jiraIssueStatuses(@RequestParam Long pdServiceLink, @RequestParam List<Long> pdServiceVersionLinks) throws Exception {
        log.info("DashboardController :: jiraIssueStatuses");
        List<AggregationResponse> result = 통계엔진통신기.제품_혹은_제품버전들의_지라이슈상태_집계(pdServiceLink, pdServiceVersionLinks);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }


}