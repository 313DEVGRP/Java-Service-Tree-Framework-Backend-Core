/*
 * @author Dongmin.lee
 * @since 2023-03-28
 * @version 23.03.28
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.jira.jiraserver_pure.controller;

import com.arms.api.jira.jiraserver.model.JiraServerDTO;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureDTO;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.jira.jiraserver_pure.service.JiraServerPure;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraServerPure"})
public class JiraServerPureController extends TreeAbstractController<JiraServerPure, JiraServerPureDTO, JiraServerPureEntity> {

    @Autowired
    @Qualifier("jiraServerPure")
    private JiraServerPure jiraServerPure;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraServerPure);
        setTreeEntity(JiraServerPureEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value= { "/getJiraServerMonitor.do"},
            method= {RequestMethod.GET}
    )
    public ResponseEntity<List<JiraServerPureEntity>> getJiraServerMonitor(JiraServerDTO jiraServerDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("JiraServerPureController :: getJiraServerMonitor");
        JiraServerPureEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerPureEntity.class);

        return ResponseEntity.ok(jiraServerPure.getNodesWithoutRoot(jiraServerEntity));
    }

    @ResponseBody
    @RequestMapping(
            value= { "/getJiraServerMonitor2.do"},
            method= {RequestMethod.GET}
    )
    public ResponseEntity<List<JiraServerPureEntity>> getJiraServerMonitor2(JiraServerDTO jiraServerDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("JiraServerController :: getJiraServerMonitor");
        JiraServerPureEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerPureEntity.class);

        return ResponseEntity.ok(jiraServerPure.getNodesWithoutRoot(jiraServerEntity));
    }
}
