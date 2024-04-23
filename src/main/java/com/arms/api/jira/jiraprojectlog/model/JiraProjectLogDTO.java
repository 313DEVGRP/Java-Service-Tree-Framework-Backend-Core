package com.arms.api.jira.jiraprojectlog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectLogDTO extends TreeBaseDTO {


    private String c_jira_contents;

    private String c_jira_etc;

    private String c_jira_url;

    private String c_jira_id;

    private String c_jira_key;

    private String c_jira_name;


    private String c_jira_avatar_48;

    private String c_jira_avatar_32;

    private String c_jira_avatar_24;

    private String c_jira_avatar_16;

    private String c_jira_category_url;

    private String c_jira_category_id;

    private String c_jira_category_name;

    private String c_jira_category_desc;

}
