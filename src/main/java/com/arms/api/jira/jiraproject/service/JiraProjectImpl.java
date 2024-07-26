/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.jira.jiraproject.service;

import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraserver.model.enums.EntityType;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("jiraProject")
public class JiraProjectImpl extends TreeServiceImpl implements JiraProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Override
    @Transactional
    public List<JiraProjectEntity> getConnectionInfo(ReqAddEntity reqAddEntity) throws Exception {
        // 지라 프로젝트 리스트 조회
        JiraProjectEntity jiraProjectEntity = new JiraProjectEntity();
        List<JiraProjectEntity> jiraProjectEntityList = this.getNodesWithoutRoot(jiraProjectEntity);

        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        List<PdServiceEntity> pdServiceEntityList = this.getNodesWithoutRoot(pdServiceEntity);

        GlobalTreeMapEntity globalTreeMapEntity = new GlobalTreeMapEntity();
        List<GlobalTreeMapEntity> 글로벌앤티티_조회_리스트 = globalTreeMapService.findAllBy(globalTreeMapEntity);

        List<JiraProjectEntity> reqNodes = new ArrayList<>();

        for (PdServiceEntity serviceEntity : pdServiceEntityList) {
            for (GlobalTreeMapEntity treeMapEntity : 글로벌앤티티_조회_리스트) {
                if (treeMapEntity.getPdservice_link() != null &&
                        treeMapEntity.getPdserviceversion_link() != null &&
                        treeMapEntity.getJiraproject_link() != null) {

                    System.out.println("treeMapEntity info:: " + treeMapEntity.getJiraproject_link());
                    List<JiraProjectEntity> filteredList = jiraProjectEntityList.stream()
                            .filter(jiraNode -> treeMapEntity.getJiraproject_link().equals(jiraNode.getC_id()))
                            .collect(Collectors.toList());

                    for (JiraProjectEntity jiraProject : filteredList) {
                        System.out.println("jiraProject::c_id:" + jiraProject.getC_id());

                        reqNodes.add(jiraProject); // 값을 List에 추가
                    }
                }
            }
            serviceEntity.getC_id();
        }
        return reqNodes;
    }

    @Override
    public List<JiraIssueTypeEntity> 프로젝트_이슈유형_리스트_조회(JiraProjectEntity jiraProjectEntity) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        List<JiraIssueTypeEntity> 결과 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities().stream()
                .filter(Objects::nonNull)
                .filter(entity ->
                        // 프로젝트별 이슈유형 조회 후 서브테스크 타입 확인 && 소프트 딜리트 처리 확인 후 반환
                        (entity.getC_desc() != null && !"true".equals(entity.getC_desc())
                                || (entity.getC_contents() != null && !"-1".equals(entity.getC_contents())))
                                && (entity.getC_etc() == null || !StringUtils.equals("delete", entity.getC_etc()))
                )
                .collect(Collectors.toList());

        return 결과;
    }

    @Override
    public List<JiraIssueStatusEntity> 프로젝트_이슈상태_리스트_조회(JiraProjectEntity jiraProjectEntity) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);
        JiraIssueTypeEntity 조회된_이슈유형 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities().stream()
                .filter(issueType -> StringUtils.equals("true", issueType.getC_check()))
                .findFirst().orElse(null);

        // 선택된 이슈유형이 없는 경우 상태 빈값으로 전달
        if (조회된_이슈유형 == null) {
             return Collections.emptyList();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String 조회된_이슈유형아이디 = 조회된_이슈유형.getC_issue_type_id();

        List<JiraIssueStatusEntity> result = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities().stream()
                .filter(Objects::nonNull)
                .filter(entity -> entity.getC_etc() == null || !StringUtils.equals("delete", entity.getC_etc()))
                .filter(entity -> entity.getC_issue_type_mapping_id() != null)
                .filter(entity -> {
                    try {
                        Set<String> 이슈유형_아이디_목록 = objectMapper.readValue(entity.getC_issue_type_mapping_id(), new TypeReference<Set<String>>() {});
                        return 이슈유형_아이디_목록.contains(조회된_이슈유형아이디);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    @Transactional
    public JiraProjectEntity 프로젝트_항목별_기본값_설정(EntityType 설정할_항목, Long 항목_c_id, JiraProjectEntity jiraProjectEntity) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        // 이슈유형
        if (EntityType.이슈유형 == 설정할_항목) {
            Set<JiraIssueTypeEntity> 이슈_유형_목록 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities();
            if (이슈_유형_목록 != null && 이슈_유형_목록.size() != 0) {
                for (JiraIssueTypeEntity 이슈_유형 : 이슈_유형_목록) {
                    if (Objects.equals(이슈_유형.getC_id(), 항목_c_id)) {
                        이슈_유형.setC_check("true");
                    } else {
                        이슈_유형.setC_check("false");
                    }
                }
            }
        }

        //이슈상태
        if (EntityType.이슈상태 == 설정할_항목) {
            Set<JiraIssueStatusEntity> 이슈_상태_목록 = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities();
            if (이슈_상태_목록 != null && 이슈_상태_목록.size() != 0 ) {
                for (JiraIssueStatusEntity 이슈_상태 : 이슈_상태_목록) {
                    if (Objects.equals(이슈_상태.getC_id(), 항목_c_id)) {
                        이슈_상태.setC_check("true");
                    } else {
                        이슈_상태.setC_check("false");
                    }
                }
            }
        }

        this.updateNode(검색된_프로젝트_엔티티);
        return 검색된_프로젝트_엔티티;
    }

    @Override
    public List<JiraIssueStatusEntity> 해당_이슈유형_이슈상태_목록_조회(JiraProjectEntity jiraProjectEntity, Long 이슈유형_아이디) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);
        JiraIssueTypeEntity 조회된_이슈유형 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities().stream()
                .filter(issueType -> Objects.equals(이슈유형_아이디, issueType.getC_id()))
                .findFirst().orElse(null);

        if (조회된_이슈유형 == null) {
            return Collections.emptyList();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String 조회된_이슈유형아이디 = 조회된_이슈유형.getC_issue_type_id();

        List<JiraIssueStatusEntity> result = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities().stream()
                .filter(Objects::nonNull)
                .filter(entity -> entity.getC_etc() == null || !StringUtils.equals("delete", entity.getC_etc()))
                .filter(entity -> entity.getC_issue_type_mapping_id() != null)
                .filter(entity -> {
                    try {
                        Set<String> 이슈유형_아이디_목록 = objectMapper.readValue(entity.getC_issue_type_mapping_id(), new TypeReference<Set<String>>() {});
                        return 이슈유형_아이디_목록.contains(조회된_이슈유형아이디);
                    }
                    catch (IOException e) {
                        logger.error(e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return result;
    }

}