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
package com.arms.api.jira.jiraserver.service;

import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject_pure.model.JiraProjectPureEntity;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface JiraServer extends TreeService {

    public List<JiraServerEntity> getNodesWithoutRoot(JiraServerEntity jiraServerEntity) throws Exception;

    public JiraServerEntity addJiraServer(JiraServerEntity jiraServerEntity) throws Exception;

    public JiraServerEntity 서버_엔티티_항목별_갱신(String 갱신할_항목, JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraProjectEntity> 서버_프로젝트_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraProjectPureEntity> 서버_프로젝트만_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraIssueTypeEntity> 서버_이슈유형_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraIssueStatusEntity> 서버_이슈상태_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraIssuePriorityEntity> 서버_이슈우선순위_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public List<JiraIssueResolutionEntity> 서버_이슈해결책_가져오기(JiraServerEntity jiraServerEntity) throws Exception;

    public JiraServerEntity 서버_항목별_기본값_설정(String 설정할_항목, Long 항목_c_id, JiraServerEntity jiraServerEntity) throws Exception;
}