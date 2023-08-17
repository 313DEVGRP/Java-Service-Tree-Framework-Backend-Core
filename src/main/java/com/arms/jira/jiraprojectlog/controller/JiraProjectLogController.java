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
package com.arms.jira.jiraprojectlog.controller;

import com.arms.jira.jiraprojectlog.model.JiraProjectLogDTO;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.jira.jiraprojectlog.model.JiraProjectLogEntity;
import com.arms.jira.jiraprojectlog.service.JiraProjectLog;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraProjectLog"})
public class JiraProjectLogController extends TreeAbstractController<JiraProjectLog, JiraProjectLogDTO, JiraProjectLogEntity> {

    @Autowired
    @Qualifier("jiraProjectLog")
    private JiraProjectLog jiraProjectLog;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraProjectLog);
        setTreeEntity(JiraProjectLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}