package com.arms.jira.jiraserver.model;

import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraServerDTO extends TreeBaseDTO {


    private String c_jira_server_name;

    private String c_jira_server_base_url;

    private String c_jira_server_type;

    private String c_jira_server_connect_id;

    private String c_jira_server_connect_pw;

    private String c_jira_server_contents;

    private String c_jira_server_etc;

    private Set<JiraProjectEntity> jiraProjectEntities;   // 추가됨

    private Set<JiraIssueTypeEntity> jiraIssueTypeEntities;

    private Set<JiraIssuePriorityEntity> jiraIssuePriorityEntities;

    private Set<JiraIssueResolutionEntity> jiraIssueResolutionEntities;

    private Set<JiraIssueStatusEntity> jiraIssueStatusEntities;


}
