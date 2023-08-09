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
package com.arms.jira.jiraissuepriority.service;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Priority;
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
@Service("jiraIssuePriority")
public class JiraIssuePriorityImpl extends TreeServiceImpl implements JiraIssuePriority{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		Iterable<Priority> priorities = restClient.getMetadataClient().getPriorities().get();

		JiraIssuePriorityEntity jiraIssuePriorityEntity = new JiraIssuePriorityEntity();
		List<JiraIssuePriorityEntity> list = this.getNodesWithoutRoot(jiraIssuePriorityEntity);

		for (Priority priority : priorities) {
			logger.info("priority -> " + priority.getName());
			logger.info("priority -> " + priority.getId());
			logger.info("priority -> " + priority.getSelf());
			logger.info("priority -> " + priority.getDescription());

			boolean anyMatch = list.stream().anyMatch(savedPriority ->
					StringUtils.equals(savedPriority.getC_issue_priority_id(), priority.getId().toString())
			);

			if(anyMatch){
				logger.info("already registerd jira priority -> " + priority.getName());
				logger.info("already registerd jira priority -> " + priority.getId());
				logger.info("already registerd jira priority -> " + priority.getSelf());
				logger.info("already registerd jira priority -> " + priority.getDescription());
				// version check ( 이미 등록된 )

			}else{

				JiraIssuePriorityEntity targetJiraIssuePriorityEntity = new JiraIssuePriorityEntity();

				targetJiraIssuePriorityEntity.setC_issue_priority_name(priority.getName());
				targetJiraIssuePriorityEntity.setC_issue_priority_id(priority.getId().toString());
				targetJiraIssuePriorityEntity.setC_issue_priority_url(priority.getSelf().toString());
				targetJiraIssuePriorityEntity.setC_issue_priority_desc(priority.getDescription());

				targetJiraIssuePriorityEntity.setRef(TreeConstant.First_Node_CID);
				targetJiraIssuePriorityEntity.setC_type(TreeConstant.Leaf_Node_TYPE);

				this.addNode(targetJiraIssuePriorityEntity);

			}
		}

		return "Jira issue priority Data Mining Complete";
	}
}