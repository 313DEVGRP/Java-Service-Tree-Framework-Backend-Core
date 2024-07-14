package com.arms.api.jira.jiraserver_pure.service;

import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.Map;

public interface JiraServerPure extends TreeService {

    Map<String, ServerType> 지라서버_아이디_타입_정보_가져오기() throws Exception;
}
