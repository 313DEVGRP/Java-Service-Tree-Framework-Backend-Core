package com.arms.jira.jiraproject.model;

import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectDTO extends TreeBaseDTO {

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

}
