package com.arms.api.jira.jiraserver_project_pure.service;

import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
            .filter(Objects::nonNull)
            .map(entity -> {
                return 엔티티별_소프트_딜리트_제외(entity);
            })
            .collect(Collectors.toList());

        return 반환목록;
    }

    @Override
    public JiraServerProjectPureEntity getNodeWithoutSoftDelete(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception {
        JiraServerProjectPureEntity result = this.getNode(jiraServerProjectPureEntity);

        if (result == null) {
            return result;
        }

        return 엔티티별_소프트_딜리트_제외(result);
    }

    public JiraServerProjectPureEntity 엔티티별_소프트_딜리트_제외(JiraServerProjectPureEntity jiraServerProjectPureEntity) {
        프로젝트_및_프로젝트별_이슈타입_소프트딜리트_제외(jiraServerProjectPureEntity);
        전역_이슈타입_소프트딜리트_제외(jiraServerProjectPureEntity);
        전역_이슈_우선순위_소프트딜리트_제외(jiraServerProjectPureEntity);

        return jiraServerProjectPureEntity;
    }

    private void 프로젝트_및_프로젝트별_이슈타입_소프트딜리트_제외(JiraServerProjectPureEntity jiraServerProjectPureEntity) {
        if (jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities() != null) {
            Set<JiraProjectIssueTypePureEntity> filteredProjects = 프로젝트_소프트딜리트_제외(jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities());
            jiraServerProjectPureEntity.setJiraProjectIssueTypePureEntities(filteredProjects);
        }
    }

    private Set<JiraProjectIssueTypePureEntity> 프로젝트_소프트딜리트_제외(Set<JiraProjectIssueTypePureEntity> projects) {
        return projects.stream()
                .filter(project -> project.getC_etc() == null
                                        || !StringUtils.equals(project.getC_etc(), "delete"))
                .peek(this::프로젝트별_이슈타입_소프트딜리트_제외)
                .collect(Collectors.toSet());
    }

    private void 프로젝트별_이슈타입_소프트딜리트_제외(JiraProjectIssueTypePureEntity project) {
        if (project.getJiraIssueTypeEntities() != null) {
            Set<JiraIssueTypeEntity> filteredIssueTypes = project.getJiraIssueTypeEntities().stream()
                    .filter(issuetype -> issuetype.getC_etc() == null
                            || !StringUtils.equals(issuetype.getC_etc(), "delete"))
                    .collect(Collectors.toSet());
            project.setJiraIssueTypeEntities(filteredIssueTypes);
        }
    }

    private void 전역_이슈타입_소프트딜리트_제외(JiraServerProjectPureEntity jiraServerProjectPureEntity) {
        if (jiraServerProjectPureEntity.getJiraIssueTypeEntities() != null) {
            Set<JiraIssueTypeEntity> filteredIssueTypes = jiraServerProjectPureEntity.getJiraIssueTypeEntities().stream()
                    .filter(issueType -> issueType.getC_etc() == null
                                            || !StringUtils.equals(issueType.getC_etc(), "delete"))
                    .collect(Collectors.toSet());
            jiraServerProjectPureEntity.setJiraIssueTypeEntities(filteredIssueTypes);
        }
    }

    private void 전역_이슈_우선순위_소프트딜리트_제외(JiraServerProjectPureEntity jiraServerProjectPureEntity) {
        if (jiraServerProjectPureEntity.getJiraIssuePriorityEntities() != null) {
            Set<JiraIssuePriorityEntity> filteredIssuePriorities = jiraServerProjectPureEntity.getJiraIssuePriorityEntities().stream()
                    .filter(issuePriority -> issuePriority.getC_etc() == null
                                                || !StringUtils.equals(issuePriority.getC_etc(), "delete"))
                    .collect(Collectors.toSet());
            jiraServerProjectPureEntity.setJiraIssuePriorityEntities(filteredIssuePriorities);
        }
    }

    @Override
    public List<JiraProjectIssueTypePureEntity> 서버_프로젝트_가져오기(JiraServerProjectPureEntity jiraServerProjectPureEntity) throws Exception {
        JiraServerProjectPureEntity result = this.getNode(jiraServerProjectPureEntity);

        if (result == null || result.getJiraProjectIssueTypePureEntities() == null) {
            return Collections.emptyList();
        }

        return result.getJiraProjectIssueTypePureEntities()
                .stream()
                .filter(project -> project.getC_etc() == null
                        || !StringUtils.equals(project.getC_etc(), "delete"))
                .collect(Collectors.toList());
    }
}
