package com.arms.api.jira.jiraissueprioritylog.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssuePriorityLogDTO extends TreeBaseDTO {


    private String c_issue_priority_id;

    private String c_issue_priority_desc;

    private String c_issue_priority_name;

    private String c_issue_priority_url;


}
