package com.arms.jira.jiraissueresolution.model;

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
public class JiraIssueResolutionDTO extends TreeBaseDTO {


    private String c_issue_resolution_id;

    private String c_issue_resolution_desc;

    private String c_issue_resolution_name;

    private String c_issue_resolution_url;

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //클라우드 대응 : private boolean isDefault;
    //비고
    private String c_etc;
}
