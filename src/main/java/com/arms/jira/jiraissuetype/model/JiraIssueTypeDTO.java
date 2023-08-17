package com.arms.jira.jiraissuetype.model;

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
public class JiraIssueTypeDTO extends TreeBaseDTO {


    //온프라미스 대응 : private final Long id;
    //클라우드 대응 : private String id;
    private String c_issue_type_id;

    //온프라미스 대응 : private final String description;
    //클라우드 대응 : private String description;
    private String c_issue_type_desc;

    //온프라미스 대응 : private final String name;
    //클라우드 대응 : private String name;
    private String c_issue_type_name;

    //온프라미스 대응 : private final URI self;
    //클라우드 대응 : private String self;
    private String c_issue_type_url;

    //내용
    //온프라미스 대응 : private String type; // 표준 이슈 유형(standard), 하위 작업 이슈 유형(subtask)
    //클라우드 대응 : private Integer hierarchyLevel;
    private String c_contents;

    //설명
    //온프라미스 대응 : private final boolean isSubtask;
    //클라우드 대응 : private Boolean subtask;
    private String c_desc;

    //비고
    //온프라미스 대응 : private final URI iconUri;
    //클라우드 대응 : private String untranslatedName;
    private String c_etc;

}