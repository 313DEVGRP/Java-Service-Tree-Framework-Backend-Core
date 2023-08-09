package com.arms.jira.jiraissuestatus.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueStatusDTO extends TreeBaseDTO {


    private String c_issue_status_id;

    private String c_issue_status_desc;

    private String c_issue_status_name;

    private String c_issue_status_url;
}
