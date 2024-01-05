package com.arms.api.jira.jiraserver_pure.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraServerPureDTO extends TreeBaseDTO {


    private String c_jira_server_name;

    private String c_jira_server_base_url;

    private String c_jira_server_type;

    private String c_jira_server_connect_id;

    private String c_jira_server_connect_pw;

    private String c_jira_server_contents;

    private String c_jira_server_etc;
}
