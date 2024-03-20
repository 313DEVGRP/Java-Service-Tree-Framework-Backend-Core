package com.arms.api.jira.jiraproject_issuetype_pure.service;

import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("jiraProjectIssueTypePure")
public class JiraProjectIssueTypePureImpl extends TreeServiceImpl implements JiraProjectIssueTypePure {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}