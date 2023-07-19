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
package com.arms.jiraissuetype.controller;

import com.arms.jiraissuetype.model.JiraIssueTypeDTO;
import com.arms.jiraissuetypelog.model.JiraIssueTypeLogEntity;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.arms.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jiraissuetype.service.JiraIssueType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraIssueType"})
public class JiraIssueTypeController extends TreeAbstractController<JiraIssueType, JiraIssueTypeDTO, JiraIssueTypeEntity> {

    @Autowired
    @Qualifier("jiraIssueType")
    private JiraIssueType jiraIssueType;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraIssueType);
        setTreeEntity(JiraIssueTypeEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/miningDataToaRMS.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> miningDataToaRMS(ModelMap model, HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(CommonResponse.success(jiraIssueType.miningDataToaRMS()));
    }

}
