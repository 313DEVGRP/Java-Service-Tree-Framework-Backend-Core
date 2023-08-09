/*
 * @author Dongmin.lee
 * @since 2023-03-26
 * @version 23.03.26
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.jira.jiraissuetype.service;

import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
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
@Service("jiraIssueType")
public class JiraIssueTypeImpl extends TreeServiceImpl implements JiraIssueType{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		Iterable<IssueType> issueTypes = restClient.getMetadataClient().getIssueTypes().get();

		JiraIssueTypeEntity jiraIssueType = new JiraIssueTypeEntity();
		List<JiraIssueTypeEntity> list = this.getNodesWithoutRoot(jiraIssueType);

		for (IssueType type : issueTypes) {
			logger.info("type -> " + type.getId());
			logger.info("type -> " + type.getName());
			logger.info("type -> " + type.getSelf());
			logger.info("type -> " + type.getDescription());

			boolean anyMatch = list.stream().anyMatch(savedType ->
					StringUtils.equals(savedType.getC_issue_type_id(), type.getId().toString())
			);

			if(anyMatch){
				logger.info("already registerd jira type -> " + type.getName());
				logger.info("already registerd jira type -> " + type.getId());
				logger.info("already registerd jira type -> " + type.getSelf());
				logger.info("already registerd jira type -> " + type.getDescription());
				// version check ( 이미 등록된 )

			}else{

				JiraIssueTypeEntity issueType = new JiraIssueTypeEntity();

				issueType.setC_issue_type_id(type.getId().toString());
				issueType.setC_issue_type_desc(type.getDescription());
				issueType.setC_issue_type_name(type.getName());
				issueType.setC_issue_type_url(type.getSelf().toString());

				issueType.setRef(TreeConstant.First_Node_CID);
				issueType.setC_type(TreeConstant.Leaf_Node_TYPE);

				this.addNode(issueType);

			}
		}

		return "Jira issue Type Data Mining Complete";
	}

}