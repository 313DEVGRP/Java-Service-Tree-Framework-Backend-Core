package com.arms.jira.jiraissuetypelog.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueTypeLogDTO extends TreeBaseDTO {


    private String c_issue_type_id;

    private String c_issue_type_desc;

    private String c_issue_type_name;

    private String c_issue_type_url;
}