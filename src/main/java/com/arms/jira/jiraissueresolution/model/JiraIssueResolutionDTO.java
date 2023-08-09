package com.arms.jira.jiraissueresolution.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueResolutionDTO extends TreeBaseDTO {


    private String c_issue_resolution_id;

    private String c_issue_resolution_desc;

    private String c_issue_resolution_name;

    private String c_issue_resolution_url;
}
