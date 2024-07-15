package com.arms.api.jira.jiraserver_pure.service;


import com.arms.api.util.communicate.external.EngineService;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@AllArgsConstructor
@Service("jiraServerPure")
public class JiraServerPureImpl extends TreeServiceImpl implements JiraServerPure {

    @Autowired
    private EngineService engineService;
    @Override
    public Map<String, String> ALM서버_아이디_서버유형_맵_가져오기() {
        return engineService.서버_연결아이디_유형정보_맵_조회();
    }
}
