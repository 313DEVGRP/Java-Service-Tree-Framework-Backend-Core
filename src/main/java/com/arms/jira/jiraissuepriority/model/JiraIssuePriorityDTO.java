package com.arms.jira.jiraissuepriority.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssuePriorityDTO extends TreeBaseDTO {


    private String c_issue_priority_id;

    private String c_issue_priority_desc;

    private String c_issue_priority_name;

    private String c_issue_priority_url;

}
