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
package com.arms.api.jira.jiraissuestatuslog.controller;

import com.arms.api.jira.jiraissuestatuslog.model.JiraIssueStatusLogDTO;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.jira.jiraissuestatuslog.model.JiraIssueStatusLogEntity;
import com.arms.api.jira.jiraissuestatuslog.service.JiraIssueStatusLog;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraIssueStatusLog"})
public class JiraIssueStatusLogController extends TreeAbstractController<JiraIssueStatusLog, JiraIssueStatusLogDTO, JiraIssueStatusLogEntity> {

    @Autowired
    @Qualifier("jiraIssueStatusLog")
    private JiraIssueStatusLog jiraIssueStatusLog;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraIssueStatusLog);
        setTreeEntity(JiraIssueStatusLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
