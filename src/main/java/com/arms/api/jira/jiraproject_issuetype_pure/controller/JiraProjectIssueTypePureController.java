package com.arms.api.jira.jiraproject_issuetype_pure.controller;

import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureDTO;
import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.api.jira.jiraproject_issuetype_pure.service.JiraProjectIssueTypePure;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraProjectIssueTypePure"})
public class JiraProjectIssueTypePureController extends TreeAbstractController<JiraProjectIssueTypePure, JiraProjectIssueTypePureDTO, JiraProjectIssueTypePureEntity> {

    @Autowired
    @Qualifier("jiraProjectIssueTypePure")
    private JiraProjectIssueTypePure jiraProjectIssueTypePure;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraProjectIssueTypePure);
        setTreeEntity(JiraProjectIssueTypePureEntity.class);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
