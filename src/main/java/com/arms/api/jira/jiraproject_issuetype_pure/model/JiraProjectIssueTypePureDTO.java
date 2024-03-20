package com.arms.api.jira.jiraproject_issuetype_pure.model;

import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectIssueTypePureDTO extends TreeBaseDTO {

    //클라우드 대응 : private String key;
    //온프라미스 대응 : private String key;
    private String c_jira_key;

    //클라우드 대응 : private String name;
    //온프라미스 대응 : private String name;
    private String c_jira_name;

    //클라우드 대응 : private String self;
    //온프라미스 대응 : private String self;
    private String c_jira_url;

    //내용
    private String c_contents;

    //클라우드 대응 : private String id;
    //온프라미스 대응 : private String id;
    //설명
    private String c_desc;

    //비고
    private String c_etc;

    private Set<JiraIssueTypeEntity> jiraIssueTypeEntities;

}
