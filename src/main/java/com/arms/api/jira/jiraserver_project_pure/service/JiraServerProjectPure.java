package com.arms.api.jira.jiraserver_project_pure.service;

import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface JiraServerProjectPure extends TreeService {
    public List<JiraServerProjectPureEntity> getChildNodeWithoutSoftDelete(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception;

}
