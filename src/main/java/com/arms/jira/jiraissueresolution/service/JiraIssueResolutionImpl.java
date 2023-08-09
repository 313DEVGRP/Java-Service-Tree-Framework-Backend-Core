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
package com.arms.jira.jiraissueresolution.service;

import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Resolution;
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
@Service("jiraIssueResolution")
public class JiraIssueResolutionImpl extends TreeServiceImpl implements JiraIssueResolution{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		Iterable<Resolution> Resolutions = restClient.getMetadataClient().getResolutions().get();

		JiraIssueResolutionEntity jiraIssueResolutionEntity = new JiraIssueResolutionEntity();
		List<JiraIssueResolutionEntity> list = this.getNodesWithoutRoot(jiraIssueResolutionEntity);

		for (Resolution resolution : Resolutions) {
			logger.info("resolution -> " + resolution.getId());
			logger.info("resolution -> " + resolution.getDescription());
			logger.info("resolution -> " + resolution.getSelf());
			logger.info("resolution -> " + resolution.getName());

			boolean anyMatch = list.stream().anyMatch(savedResolution ->
					StringUtils.equals(savedResolution.getC_issue_resolution_id(), resolution.getId().toString())
			);

			if(anyMatch){
				logger.info("already registerd jira resolution -> " + resolution.getName());
				logger.info("already registerd jira resolution -> " + resolution.getId());
				logger.info("already registerd jira resolution -> " + resolution.getSelf());
				logger.info("already registerd jira resolution -> " + resolution.getDescription());
				// version check ( 이미 등록된 )

			}else{

				JiraIssueResolutionEntity jiraIssueResolution = new JiraIssueResolutionEntity();

				jiraIssueResolution.setC_issue_resolution_name(resolution.getName());
				jiraIssueResolution.setC_issue_resolution_id(resolution.getId().toString());
				jiraIssueResolution.setC_issue_resolution_url(resolution.getSelf().toString());
				jiraIssueResolution.setC_issue_resolution_desc(resolution.getDescription());

				jiraIssueResolution.setRef(TreeConstant.First_Node_CID);
				jiraIssueResolution.setC_type(TreeConstant.Leaf_Node_TYPE);

				this.addNode(jiraIssueResolution);

			}
		}

		return "Jira issue Resolution Data Mining Complete";
	}
}