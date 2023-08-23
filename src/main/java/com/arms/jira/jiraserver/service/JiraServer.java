/*
 * @author Dongmin.lee
 * @since 2023-03-28
 * @version 23.03.28
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.jira.jiraserver.service;

import com.arms.jira.jiraserver.model.JiraServerDTO;
import com.arms.jira.jiraserver.model.JiraServerEntity;
import com.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface JiraServer extends TreeService {

    public List<JiraServerEntity> getNodesWithoutRoot(JiraServerEntity jiraServerEntity) throws Exception;

    public JiraServerEntity addJiraServer(JiraServerEntity jiraServerEntity) throws Exception;

    public JiraServerEntity 서버_엔티티_이슈_상태_갱신(String 갱신할_항목, JiraServerEntity jiraServerEntity) throws Exception;
}