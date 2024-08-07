package com.arms.api.jira.jiraissuestatus.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueStatusDTO extends TreeBaseDTO {


    private String c_issue_status_id;

    private String c_issue_status_desc;

    private String c_issue_status_name;

    private String c_issue_status_url;

    private String c_issue_type_mapping_id;

    //값으로 : true, false 를 가질 수 있다.
    private String c_check;

    //내용
    private String c_contents;

    //설명
    private String c_desc;

    //비고
    private String c_etc;

    // ARMS 요구사항 상태 mapping
    private Long c_req_state_mapping_link;
}
