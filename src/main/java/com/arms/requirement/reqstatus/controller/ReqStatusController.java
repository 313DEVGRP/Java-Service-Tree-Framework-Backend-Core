/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.requirement.reqstatus.controller;

import com.arms.jira.jiraissuepriority.service.JiraIssuePriority;
import com.arms.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.jira.jiraserver.service.JiraServer;
import com.arms.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.util.external_communicate.dto.지라이슈;
import com.arms.util.external_communicate.엔진통신기;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.arms.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.requirement.reqstatus.service.ReqStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqStatus"})
public class ReqStatusController extends TreeAbstractController<ReqStatus, ReqStatusDTO, ReqStatusEntity> {

    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;

    @Autowired
    @Qualifier("jiraIssuePriority")
    private JiraIssuePriority jiraIssuePriority;

    @Autowired
    @Qualifier("jiraIssueStatus")
    private JiraIssueStatus jiraIssueStatus;

    @PostConstruct
    public void initialize() {
        setTreeService(reqStatus);
        setTreeEntity(ReqStatusEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/addStatusNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> 요구사항_이슈_저장하기(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @RequestBody ReqStatusDTO reqStatusDTO) throws Exception {

        log.info("ReqStatusController :: addStatusNode");
        ReqStatusEntity reqStatusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("addStatusNode",changeReqTableName);

        ReqStatusEntity savedNode = reqStatus.addNode(reqStatusEntity);

        SessionUtil.removeAttribute("addStatusNode");

        log.info("ReqStatusController :: addStatusNode");
        return ResponseEntity.ok(CommonResponse.success(savedNode));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getStatusMonitor.do"},
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ModelAndView getMonitor(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getStatusMonitor");
        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("getStatusMonitor",changeReqTableName);

        statusEntity.setOrder(Order.asc("c_id"));

        List<ReqStatusEntity> list = reqStatus.getNodesWithoutRoot(statusEntity);

        SessionUtil.removeAttribute("getStatusMonitor");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getStatistics.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getStatistics(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getStatistics");
        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("getStatistics",changeReqTableName);

        statusEntity.setOrder(Order.asc("c_left"));

        List<ReqStatusEntity> list = reqStatus.getNodesWithoutRoot(statusEntity);

        List<Long> versionList = list.stream()
                .map(ReqStatusEntity::getC_pds_version_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> jiraServerList = list.stream()
                .map(ReqStatusEntity::getC_jira_server_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> jiraProjectList = list.stream()
                .map(ReqStatusEntity::getC_jira_project_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> reqList = list.stream()
                .map(ReqStatusEntity::getC_req_link)
                .distinct()
                .collect(Collectors.toList());

        List<String> issueList = list.stream()
                .map(ReqStatusEntity::getC_issue_key)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put("version", versionList.size());
        result.put("jiraServer", jiraServerList.size());
        result.put("jiraProject", jiraProjectList.size());
        result.put("req", reqList.size());
        result.put("issue", issueList.size());

        SessionUtil.removeAttribute("getStatistics");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    @Autowired
    private com.arms.util.external_communicate.엔진통신기 엔진통신기;

    static final long dummy_jira_server = 0L;
    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getProgress.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getProgress(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", "");
        Long pdService = Long.parseLong(pdServiceStr);

        ParameterParser parser = new ParameterParser(request);
        Long pds_version = parser.getLong("version");
        if(pds_version == null){
            pds_version = 0L;
        }

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 엔진통신기.제품서비스_버전별_상태값_통계(dummy_jira_server, pdService, pds_version));
        return modelAndView;

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getIssueAndSubLinks.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getLinkedIssueAndSubtask(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getLinkedIssueAndSubtask");

        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);
        statusEntity.setOrder(Order.asc("c_left"));

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", "");

        String 서버_아이디 = request.getParameter("serverId");
        JiraServerPureEntity 검색용_지라서버 = new JiraServerPureEntity();
        검색용_지라서버.setC_id(Long.parseLong(서버_아이디));
        JiraServerPureEntity 검색결과 = jiraServer.getNode(검색용_지라서버);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        if (검색결과 != null) {
            String 엔진통신_아이디 = 검색결과.getC_jira_server_etc();

            Long 제품서비스_아이디 = Long.parseLong(pdServiceStr);
            Long 제품서비스_버전 = Long.parseLong(request.getParameter("versionId"));
            String 이슈키 = request.getParameter("issueKey");

            int 페이지 = 0; int 사이즈 = 10;

            int 이슈_검색엔진_벌크_저장 = 엔진통신기.이슈_검색엔진_벌크_저장(Long.parseLong(엔진통신_아이디), 이슈키, 제품서비스_아이디, 제품서비스_버전);
            log.info("ReqStatusEntity :: getLinkedIssueAndSubtask => 이슈_검색엔진_벌크_저장 사이즈 = {}", StringUtils.toString(이슈_검색엔진_벌크_저장));

            List<지라이슈> 링크드이슈_서브데스크 = 엔진통신기.지라_연결된이슈_서브테스크_가져오기(Long.parseLong(엔진통신_아이디), 이슈키, 0, 10);

            log.info("ReqStatusEntity :: getLinkedIssueAndSubtask => 링크드이슈_서브데스크 = {}", 링크드이슈_서브데스크.toString());

            modelAndView.addObject("result", 링크드이슈_서브데스크);
        } else {
            modelAndView.addObject("result", "");
            log.info("ReqStatusEntity :: getLinkedIssueAndSubtask => 검색된 지라서버가 없습니다.");
        }

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getPdReqStats.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getPdReqStats(@PathVariable(value ="changeReqTableName") String changeReqTableName, HttpServletRequest request) {

        log.info("ReqStatusController :: getPdReqStats");

        Long 제품서비스_아이디 = Long.parseLong(StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", ""));
        String 담당자_이메일 = request.getParameter("assigneeEmail");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 엔진통신기.제품서비스별_담당자_요구사항_통계(dummy_jira_server, 제품서비스_아이디, 담당자_이메일));

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getPdRelatedReqStats.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getPdRelatedReqStats(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                     ReqStatusDTO reqStatusDTO, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getPdRelatedReqStats");

        Long 제품서비스_아이디 = Long.parseLong(StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", ""));

        JiraServerPureEntity 검색용_지라서버 = new JiraServerPureEntity();
        검색용_지라서버.setC_id(reqStatusDTO.getC_jira_server_link());
        JiraServerPureEntity 검색결과_지라서버 = jiraServer.getNode(검색용_지라서버);

        ReqStatusEntity reqStatusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);
        SessionUtil.setAttribute("getPdRelatedReqStats", changeReqTableName);

        Criterion searchService = Restrictions.eq("c_pdservice_link", 제품서비스_아이디);
        Criterion searchReq = Restrictions.eq("c_req_link", reqStatusDTO.getC_req_link());
        Criterion criterion = Restrictions.and(searchService, searchReq);
        reqStatusEntity.getCriterions().add(criterion);
        ReqStatusEntity 검색결과_요구사항 = reqStatus.getNode(reqStatusEntity);

        Long 지라서버_아이디 = Long.parseLong(검색결과_지라서버.getC_jira_server_etc());
        String 이슈키 = 검색결과_요구사항.getC_issue_key();
        String 담당자_이메일 = request.getParameter("assigneeEmail");

        log.info("지라서버_아이디: " + 지라서버_아이디);
        log.info("제품서비스_아이디: " + 제품서비스_아이디);
        log.info("이슈키: " + 이슈키);
        log.info("담당자_이메일: " + 담당자_이메일);

        SessionUtil.removeAttribute("getPdRelatedReqStats");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 엔진통신기.제품서비스별_담당자_연관된_요구사항_통계(지라서버_아이디, 제품서비스_아이디, 이슈키, 담당자_이메일));

        return modelAndView;
    }

}
