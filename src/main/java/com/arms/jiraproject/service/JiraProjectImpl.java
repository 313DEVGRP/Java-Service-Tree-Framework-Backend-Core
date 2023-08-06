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
package com.arms.jiraproject.service;

import com.arms.jiraproject.model.JiraProjectEntity;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.config.JiraConfig;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service("jiraProject")
public class JiraProjectImpl extends TreeServiceImpl implements JiraProject{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("jiraConfig")
	private JiraConfig jiraConfig;

	@Override
	@Transactional
	public String miningDataToaRMS() throws Exception {
		final JiraRestClient restClient = jiraConfig.getJiraRestClient();
		Iterable<BasicProject> allProject = restClient.getProjectClient().getAllProjects().claim();

		JiraProjectEntity jiraProjectEntity = new JiraProjectEntity();
		jiraProjectEntity.setC_id(TreeConstant.First_Node_CID);
		List<JiraProjectEntity> list = this.getChildNode(jiraProjectEntity);

		for (BasicProject project: allProject) {
			logger.info("project -> " + project.getName());
			logger.info("project -> " + project.getKey());
			logger.info("project -> " + project.getSelf());

			boolean anyMatch = list.stream().anyMatch(savedProject ->
					StringUtils.equals(savedProject.getC_jira_key(), project.getKey())
			);

			if(anyMatch){
				logger.info("already registerd jira project = " + project.getSelf().toString());

				// version check ( 이미 등록된 )

			}else{

				String jira_Project_KEY = project.getKey();
				Project projectDetail = restClient.getProjectClient().getProject(jira_Project_KEY).claim();

				JiraProjectEntity jiraProject = new JiraProjectEntity();
				jiraProject.setC_title(projectDetail.getName());

				jiraProject.setC_jira_url(projectDetail.getSelf().toString());
				jiraProject.setC_jira_key(projectDetail.getKey());
				jiraProject.setC_jira_name(projectDetail.getName());

				jiraProject.setRef(TreeConstant.First_Node_CID);
				jiraProject.setC_type(TreeConstant.Leaf_Node_TYPE);

				this.addNode(jiraProject);

				//version setting
				Iterable<Version> jiraProjectList = projectDetail.getVersions();

				for ( Version jiraProjectVersion : jiraProjectList ){



				}
			}
		}

		return "Jira Project Data Mining Complete";
	}


}