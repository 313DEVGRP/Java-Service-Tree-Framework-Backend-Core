package com.arms.api.jira.jiraissuepriority.model;

import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
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
