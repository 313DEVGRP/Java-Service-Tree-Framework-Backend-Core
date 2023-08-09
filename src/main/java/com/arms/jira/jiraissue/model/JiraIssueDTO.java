package com.arms.jira.jiraissue.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueDTO extends TreeBaseDTO {

    private String c_issue_id;

    private String c_issue_url;

    private String c_issue_desc;

    private String c_issue_key;

    private String c_issue_summary;

    private String c_issue_type;

    private String c_issue_labels;

    private String c_issue_components;

    private String c_issue_link_yn;

    private String c_issue_subtask_yn;

    private String c_issue_affected_versions;

    private String c_issue_fix_versions;

    private String c_issue_create_date;

    private String c_issue_update_date;

    private String c_issue_due_date;

    private Long c_issue_status;

    private Long c_issue_priority;

    private Long c_issue_resolution;
}
