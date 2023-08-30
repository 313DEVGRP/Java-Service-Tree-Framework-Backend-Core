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
package com.arms.jira.jiraproject.service;

import com.arms.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.globaltreemap.service.GlobalTreeMapService;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jira.jiraissuetype.service.JiraIssueType;
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("jiraProject")
public class JiraProjectImpl extends TreeServiceImpl implements JiraProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Autowired
    @Qualifier("jiraIssueType")
    private JiraIssueType jiraIssueType;

    @Autowired
    @Qualifier("jiraIssueStatus")
    private JiraIssueStatus jiraIssueStatus;

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
        return 검색된_프로젝트_엔티티.getJiraIssueTypeEntities().stream().collect(Collectors.toList());
    }

    @Override
    public List<JiraIssueStatusEntity> 프로젝트_이슈상태_리스트_조회(JiraProjectEntity jiraProjectEntity) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);
        return 검색된_프로젝트_엔티티.getJiraIssueStatusEntities().stream().collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<JiraIssueTypeEntity> 이슈유형_기본값_설정(JiraProjectEntity jiraProjectEntity, Long 이슈유형_c_id) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        Set<JiraIssueTypeEntity> 프로젝트_이슈유형_목록 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities();
        // 기본값으로 설정할 프로젝트의 이슈유형만 true 로 수정, 나머지는 false로 수정
        for(JiraIssueTypeEntity 이슈유형 : 프로젝트_이슈유형_목록) {
            if(이슈유형.getC_id() == 이슈유형_c_id) {
                이슈유형.setC_check("true");
            } else {
                이슈유형.setC_check("false");
            }
            jiraIssueType.updateNode(이슈유형);
        }

        this.updateNode(검색된_프로젝트_엔티티);
        return 검색된_프로젝트_엔티티.getJiraIssueTypeEntities().stream().collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<JiraIssueStatusEntity> 이슈상태_기본값_설정(JiraProjectEntity jiraProjectEntity, Long 이슈상태_c_id) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        Set<JiraIssueStatusEntity> 프로젝트_이슈상태_목록 = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities();
        for(JiraIssueStatusEntity 이슈상태 : 프로젝트_이슈상태_목록) {
            if (이슈상태.getC_id() == 이슈상태_c_id) {
                이슈상태.setC_check("true");
            } else {
                이슈상태.setC_check("false");
            }
            jiraIssueStatus.updateNode(이슈상태);
        }

        this.updateNode(검색된_프로젝트_엔티티);
        return 검색된_프로젝트_엔티티.getJiraIssueStatusEntities().stream().collect(Collectors.toList());

    }
}