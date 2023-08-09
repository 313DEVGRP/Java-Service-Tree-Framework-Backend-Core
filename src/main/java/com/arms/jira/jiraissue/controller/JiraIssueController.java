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
package com.arms.jira.jiraissue.controller;

import com.arms.jira.jiraissue.model.JiraIssueDTO;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.jira.jiraissue.model.JiraIssueEntity;
import com.arms.jira.jiraissue.service.JiraIssue;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraIssue"})
public class JiraIssueController extends TreeAbstractController<JiraIssue, JiraIssueDTO, JiraIssueEntity> {

    @Autowired
    @Qualifier("jiraIssue")
    private JiraIssue jiraIssue;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraIssue);
        setTreeEntity(JiraIssueEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/makeIssueForReqAdd.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> makeIssueForReqAdd(@Validated({AddNode.class}) JiraIssueDTO jiraIssueDTO,
                                              BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("JiraIssueController :: makeIssueForReqAdd");
        JiraIssueEntity jiraIssueEntity = modelMapper.map(jiraIssueDTO, JiraIssueEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraIssue.makeIssueForReqAdd(jiraIssueEntity)));
    }

}
