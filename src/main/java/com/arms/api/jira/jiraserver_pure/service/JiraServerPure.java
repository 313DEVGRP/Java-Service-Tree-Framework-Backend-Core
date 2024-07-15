package com.arms.api.jira.jiraserver_pure.service;

import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.Map;

public interface JiraServerPure extends TreeService {

    Map<String, String> ALM서버_아이디_서버유형_맵_가져오기();
}
