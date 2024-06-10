package com.arms.api.jira.jiraserver.model;

import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
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

    private String c_server_contents_text_formatting_type;

    private Set<JiraProjectEntity> jiraProjectEntities;   // 추가됨

    private Set<JiraIssueTypeEntity> jiraIssueTypeEntities;

    private Set<JiraIssuePriorityEntity> jiraIssuePriorityEntities;

    private Set<JiraIssueResolutionEntity> jiraIssueResolutionEntities;

    private Set<JiraIssueStatusEntity> jiraIssueStatusEntities;


}
