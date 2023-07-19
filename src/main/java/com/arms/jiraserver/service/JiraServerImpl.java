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
package com.arms.jiraserver.service;

import com.arms.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jiraserver.model.JiraServerEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.config.JiraConfig;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@AllArgsConstructor
@Service("jiraServer")
public class JiraServerImpl extends TreeServiceImpl implements JiraServer{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		ServerInfo serverInfo = restClient.getMetadataClient().getServerInfo().get();

		JiraServerEntity searchNode = new JiraServerEntity();
		List<JiraServerEntity> list = this.getNodesWithoutRoot(searchNode);

		if ( list.isEmpty() ){
			JiraServerEntity jiraServerEntity = new JiraServerEntity();
			jiraServerEntity.setC_jira_server_contents(serverInfo.getServerTime().toString());
			jiraServerEntity.setC_jira_server_etc(serverInfo.getScmInfo());
			jiraServerEntity.setC_jira_server_base_url(serverInfo.getBaseUri().toString());
			jiraServerEntity.setC_jira_server_version(serverInfo.getVersion());
			jiraServerEntity.setC_jira_server_build(StringUtils.toString(serverInfo.getBuildNumber()));
			jiraServerEntity.setC_jira_server_title(serverInfo.getServerTitle());

			jiraServerEntity.setRef(TreeConstant.First_Node_CID);
			jiraServerEntity.setC_type(TreeConstant.Leaf_Node_TYPE);

			this.addNode(jiraServerEntity);

		} else if (list.size() > 1){

			logger.info("JiraServerImpl :: miningDataToaRMS :: jira server count > 1");

		} else {

			JiraServerEntity jiraServerEntity = list.get(0);

			jiraServerEntity.setC_jira_server_contents(serverInfo.getServerTime().toString());
			jiraServerEntity.setC_jira_server_etc(serverInfo.getScmInfo());
			jiraServerEntity.setC_jira_server_base_url(serverInfo.getBaseUri().toString());
			jiraServerEntity.setC_jira_server_version(serverInfo.getVersion());
			jiraServerEntity.setC_jira_server_build(StringUtils.toString(serverInfo.getBuildNumber()));
			jiraServerEntity.setC_jira_server_title(serverInfo.getServerTitle());

			this.updateNode(jiraServerEntity);

		}

		return "Jira Server Data Mining Complete";
	}


}