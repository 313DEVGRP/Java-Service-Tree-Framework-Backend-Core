package com.arms.api.jira.jiraserver_project_pure.model;

import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class JiraServerProjectPureDTO extends TreeBaseDTO {


    private String c_jira_server_name;

    private String c_jira_server_base_url;

    private String c_jira_server_type;

    private String c_jira_server_connect_id;

    private String c_jira_server_connect_pw;

    private String c_jira_server_contents;

    private String c_jira_server_etc;

    // private Set<JiraProjectPureEntity> jiraProjectPureEntities;

    private Set<JiraProjectIssueTypePureEntity> jiraProjectIssueTypePureEntities;
}
