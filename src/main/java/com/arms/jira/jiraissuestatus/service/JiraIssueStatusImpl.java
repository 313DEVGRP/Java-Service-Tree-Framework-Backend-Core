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
package com.arms.jira.jiraissuestatus.service;

import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Status;
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
@Service("jiraIssueStatus")
public class JiraIssueStatusImpl extends TreeServiceImpl implements JiraIssueStatus{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		Iterable<Status> statuses = restClient.getMetadataClient().getStatuses().get();

		JiraIssueStatusEntity jiraIssueStatusEntity = new JiraIssueStatusEntity();
		List<JiraIssueStatusEntity> list = this.getNodesWithoutRoot(jiraIssueStatusEntity);

		for (Status status : statuses) {
			logger.info("status -> " + status.getId());
			logger.info("status -> " + status.getName());
			logger.info("status -> " + status.getSelf());
			logger.info("status -> " + status.getDescription());

			boolean anyMatch = list.stream().anyMatch(savedResolution ->
					StringUtils.equals(savedResolution.getC_issue_status_id(), status.getId().toString())
			);

			if(anyMatch){
				logger.info("already registerd jira resolution -> " + status.getName());
				logger.info("already registerd jira resolution -> " + status.getId());
				logger.info("already registerd jira resolution -> " + status.getSelf());
				logger.info("already registerd jira resolution -> " + status.getDescription());
				// version check ( 이미 등록된 )

			}else{

				JiraIssueStatusEntity issueStatus = new JiraIssueStatusEntity();

				issueStatus.setC_issue_status_id(status.getId().toString());
				issueStatus.setC_issue_status_name(status.getName());
				issueStatus.setC_issue_status_url(status.getSelf().toString());
				issueStatus.setC_issue_status_desc(status.getDescription());

				issueStatus.setRef(TreeConstant.First_Node_CID);
				issueStatus.setC_type(TreeConstant.Leaf_Node_TYPE);

				this.addNode(issueStatus);

			}
		}

		return "Jira issue Status Data Mining Complete";
	}

}