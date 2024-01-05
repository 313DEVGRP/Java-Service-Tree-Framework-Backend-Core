/*
 * @author Dongmin.lee
 * @since 2023-03-26
 * @version 23.03.26
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.jira.jiraissueresolutionlog.controller;

import com.arms.api.jira.jiraissueresolutionlog.model.JiraIssueResolutionLogDTO;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.jira.jiraissueresolutionlog.model.JiraIssueResolutionLogEntity;
import com.arms.api.jira.jiraissueresolutionlog.service.JiraIssueResolutionLog;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraIssueResolutionLog"})
public class JiraIssueResolutionLogController extends TreeAbstractController<JiraIssueResolutionLog, JiraIssueResolutionLogDTO, JiraIssueResolutionLogEntity> {

    @Autowired
    @Qualifier("jiraIssueResolutionLog")
    private JiraIssueResolutionLog jiraIssueResolutionLog;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraIssueResolutionLog);
        setTreeEntity(JiraIssueResolutionLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
