package com.arms.api.jira.jiraserver_project_pure.service;

import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("jiraServerProjectPure")
public class JiraServerProjectPureImpl extends TreeServiceImpl implements JiraServerProjectPure {

    @Override
    public List<JiraServerProjectPureEntity> getChildNodeWithoutSoftDelete(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception {

        List<JiraServerProjectPureEntity> list = this.getChildNode(jiraServerProjectPureEntity);

        List<JiraServerProjectPureEntity> 반환목록 = list.stream()
            .map(서버엔티티 -> {
                Set<JiraProjectIssueTypePureEntity> 프로젝트리스트 = 서버엔티티.getJiraProjectIssueTypePureEntities().stream()
                        .filter(Objects::nonNull)
                        .filter(projectEntity -> !StringUtils.equals("delete", projectEntity.getC_etc()))
                        .collect(Collectors.toSet());

                서버엔티티.setJiraProjectIssueTypePureEntities(프로젝트리스트);
                return 서버엔티티;
            })
            .collect(Collectors.toList());

        return 반환목록;
    }
}
