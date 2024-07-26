/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.jira.jiraproject.service;

import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraserver.model.enums.EntityType;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;

public interface JiraProject extends TreeService {

    public List<JiraProjectEntity> getConnectionInfo(ReqAddEntity reqAddEntity) throws Exception;

    public List<JiraIssueTypeEntity> 프로젝트_이슈유형_리스트_조회(JiraProjectEntity jiraProjectEntity) throws Exception;

    public List<JiraIssueStatusEntity> 프로젝트_이슈상태_리스트_조회(JiraProjectEntity jiraProjectEntity) throws Exception;

    public JiraProjectEntity 프로젝트_항목별_기본값_설정(EntityType 설정할_항목, Long 항목_c_id, JiraProjectEntity jiraProjectEntity) throws Exception;

    public List<JiraIssueStatusEntity> 해당_이슈유형_이슈상태_목록_조회(JiraProjectEntity jiraProjectEntity, Long 이슈유형_아이디) throws Exception;

}