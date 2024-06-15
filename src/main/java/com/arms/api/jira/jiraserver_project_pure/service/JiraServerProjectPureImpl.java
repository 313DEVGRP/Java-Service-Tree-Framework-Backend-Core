package com.arms.api.jira.jiraserver_project_pure.service;

import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject_issuetype_pure.model.JiraProjectIssueTypePureEntity;
import com.arms.api.jira.jiraserver.model.enums.ServerType;
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
        if (StringUtils.equals(ServerType.JIRA_CLOUD.getType(), jiraServerProjectPureEntity.getC_jira_server_type())) {
            if (jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities() != null) {
                Set<JiraProjectIssueTypePureEntity> filteredProjects = jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities().stream()
                        .filter(project -> project.getC_etc() == null
                                                        || !StringUtils.equals(project.getC_etc(), "delete"))
                        .collect(Collectors.toSet());

                jiraServerProjectPureEntity.setJiraProjectIssueTypePureEntities(filteredProjects);
            }
        }
        else if(StringUtils.equals(ServerType.JIRA_ON_PREMISE.getType(), jiraServerProjectPureEntity.getC_jira_server_type())) {
            if (jiraServerProjectPureEntity.getJiraIssueTypeEntities() != null) {
                Set<JiraIssueTypeEntity> filteredIssueTypes = jiraServerProjectPureEntity.getJiraIssueTypeEntities().stream()
                        .filter(issueType -> issueType.getC_etc() == null
                                                        || !StringUtils.equals(issueType.getC_etc(), "delete"))
                        .collect(Collectors.toSet());

                jiraServerProjectPureEntity.setJiraIssueTypeEntities(filteredIssueTypes);
            }
        }
        else if (StringUtils.equals(ServerType.REDMINE_ON_PREMISE.getType(), jiraServerProjectPureEntity.getC_jira_server_type()) ) {
            if (jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities() != null) {
                Set<JiraProjectIssueTypePureEntity> filteredProjects = jiraServerProjectPureEntity.getJiraProjectIssueTypePureEntities().stream()
                        .filter(project -> project.getC_etc() == null
                                                        || !StringUtils.equals(project.getC_etc(), "delete"))
                        .collect(Collectors.toSet());

                jiraServerProjectPureEntity.setJiraProjectIssueTypePureEntities(filteredProjects);
            }

            if (jiraServerProjectPureEntity.getJiraIssuePriorityEntities() != null) {

                Set<JiraIssuePriorityEntity> filteredIssuePriorities = jiraServerProjectPureEntity.getJiraIssuePriorityEntities().stream()
                        .filter(issuePriority -> issuePriority.getC_etc() == null
                                                        || !StringUtils.equals(issuePriority.getC_etc(), "delete"))
                        .collect(Collectors.toSet());

                jiraServerProjectPureEntity.setJiraIssuePriorityEntities(filteredIssuePriorities);
            }
        }

        return jiraServerProjectPureEntity;
    }
}
