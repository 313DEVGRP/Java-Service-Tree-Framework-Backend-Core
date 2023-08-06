package com.arms.jiraproject.model;

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
public class JiraProjectDTO extends TreeBaseDTO {

    private String c_jira_key;

    private String c_jira_name;

    private String c_jira_url;

    //내용
    private String c_jira_contents;

    //설명
    private String c_jira_desc;

    //비고
    private String c_jira_etc;

}
