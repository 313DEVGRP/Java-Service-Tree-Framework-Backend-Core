package com.arms.api.jira.jiraissueresolution.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueResolutionDTO extends TreeBaseDTO {


    private String c_issue_resolution_id;

    private String c_issue_resolution_desc;

    private String c_issue_resolution_name;

    private String c_issue_resolution_url;

    //값으로 : true, false 를 가질 수 있다.
    private String c_check;

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //클라우드 대응 : private boolean isDefault;
    //비고
    private String c_etc;
}
