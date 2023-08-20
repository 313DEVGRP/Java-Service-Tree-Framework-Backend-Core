package com.arms.jira.jiraissuestatus.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Lob;

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

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //비고
    private String c_etc;
}
