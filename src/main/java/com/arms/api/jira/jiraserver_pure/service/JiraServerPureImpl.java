package com.arms.api.jira.jiraserver_pure.service;

import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service("jiraServerPure")
public class JiraServerPureImpl extends TreeServiceImpl implements JiraServerPure{

    @Override
    public Map<String, ServerType> 지라서버_아이디_타입_정보_가져오기() throws Exception {
        JiraServerPureEntity 검색용도 = new JiraServerPureEntity();
        List<JiraServerPureEntity> 전체_서버정보 = this.getNodesWithoutRoot(검색용도);

        Map<String, ServerType> ALM서버_서버타입_맵 = new HashMap<>();
        for (JiraServerPureEntity entity : 전체_서버정보) {
            String 서버_연결_정보 = entity.getC_jira_server_etc();
            ServerType serverTypeEnum = ServerType.fromString(entity.getC_jira_server_type());

            ALM서버_서버타입_맵.put(서버_연결_정보, serverTypeEnum);
        }
        return ALM서버_서버타입_맵;
    }

}
