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

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissuepriority.service.JiraIssuePriority;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.requirement.reqadd.model.ReqAddDTO;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.arms.requirement.reqstatus.model.ReqStatusDTO;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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


}
