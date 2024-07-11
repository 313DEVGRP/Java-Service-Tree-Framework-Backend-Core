package com.arms.api.jira.jiraserver_project_pure.service;

import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface JiraServerProjectPure extends TreeService {
    public List<JiraServerProjectPureEntity> getChildNodeWithoutSoftDelete(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception;

    public JiraServerProjectPureEntity getNodeWithoutSoftDelete(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception;

    public List<JiraProjectIssueTypePureEntity> 서버_프로젝트_가져오기(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception;
}
