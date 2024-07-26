package com.arms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RouteTableConfig {
    @Bean
    public Map<String, String> reqAddRoute() {
        Map<String, String> map = new HashMap<>();
        map.put("getMonitor.do", "getMonitor");
        map.put("getNode.do", "getNode");
        map.put("getNodeDetail.do", "getNodeDetail");
        map.put("getDetail.do", "getDetail");
        map.put("getChildNode.do", "getChildNode");
        map.put("getNodesWithoutRoot.do", "getNodesWithoutRoot");
        map.put("getChildNodeWithParent.do", "getChildNodeWithParent");
        map.put("addNode.do", "addNode");
        map.put("addFolderNode.do", "addReqFolderNode");
        map.put("updateNode.do", "updateNode");
        map.put("updateDate.do", "updateDate");
        map.put("updateDataBase.do", "updateDataBase");
        map.put("getNodesWhereInIds.do", "getNodesWhereInIds");
        map.put("removeNode.do", "removeNode");
        map.put("moveNode.do", "moveNode");
        map.put("getHistory.do", "getHistory");
        map.put("getReqAddListByFilter.do", "getReqAddListByFilter");
        map.put("reqProgress.do", "reqProgress");
        map.put("req-difficulty-priority-list", "req-difficulty-priority-list");
        map.put("updateReqAddOnly.do", "updateReqAddOnly");
        map.put("updateDrawIOContents.do", "updateDrawIOContents");
        map.put("updateDrawDBContents.do", "updateDrawDBContents");
        return map;
    }

    @Bean
    public Map<String, String> reqStatusRoute() {
        Map<String, String> map = new HashMap<>();
        map.put("getStatusMonitor.do", "getStatusMonitor");
        map.put("getStatistics.do", "getStatistics");
        map.put("getStatusNode.do", "getStatusNode");
        map.put("getStatusChildNode.do", "getStatusChildNode");
        map.put("getStatusChildNodeWithParent.do", "getStatusChildNodeWithParent");
        map.put("addStatusNode.do", "addStatusNode");
        map.put("updateStatusNode.do", "updateStatusNode");
        map.put("removeStatusNode.do", "removeStatusNode");
        map.put("updateDate.do", "updateDate");
        map.put("updateDataBase.do", "updateDataBase");
        map.put("removeNode.do", "removeNode");
        map.put("moveStatusNode.do", "moveStatusNode");
        map.put("getStatusHistory.do", "getStatusHistory");
        map.put("getPdRelatedReqStats.do", "getPdRelatedReqStats");
        map.put("getReqStatusListByCReqLink.do", "getReqStatusListByCReqLink");
        map.put("reqStatusCheckAfterAlmProcess.do","reqStatusCheckAfterAlmProcess");

        return map;
    }

    @Bean
    public Map<String, String> reqLinkedIssueRoute() {
        Map<String, String> map = new HashMap<>();
        map.put("req-linked-issue", "req-linked-issue");
        return map;
    }
}
