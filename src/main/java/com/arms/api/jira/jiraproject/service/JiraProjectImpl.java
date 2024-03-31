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
import com.arms.api.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraissuetype.service.JiraIssueType;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.jira.jiraserver_pure.service.JiraServerPure;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.util.communicate.external.response.jira.지라이슈상태_데이터;
import com.arms.api.util.communicate.external.response.jira.지라이슈유형_데이터;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service("jiraProject")
public class JiraProjectImpl extends TreeServiceImpl implements JiraProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private 엔진통신기 엔진통신기;

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Autowired
    @Qualifier("jiraServerPure")
    private JiraServerPure jiraServerPure;
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
        return 검색된_프로젝트_엔티티.getJiraIssueStatusEntities().stream()
                .filter(entity -> entity.getC_etc() == null
                                    || !StringUtils.equals("delete", entity.getC_etc()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JiraProjectEntity 프로젝트_항목별_기본값_설정(String 설정할_항목, Long 항목_c_id, JiraProjectEntity jiraProjectEntity) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        if(설정할_항목.equals("issueType")) {
            Set<JiraIssueTypeEntity> 이슈_유형_목록 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities();
            if(이슈_유형_목록.size() != 0) {
                for (JiraIssueTypeEntity 이슈_유형 : 이슈_유형_목록) {
                    if (Objects.equals(이슈_유형.getC_id(), 항목_c_id)) {
                        이슈_유형.setC_check("true");
                    } else {
                        이슈_유형.setC_check("false");
                    }
                }
            }
        }
        //이슈 상태
        if(설정할_항목.equals("issueStatus")) {
            Set<JiraIssueStatusEntity> 이슈_상태_목록 = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities();
            if(이슈_상태_목록.size() != 0 ) {
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
    @Transactional
    public JiraProjectEntity 프로젝트_항목별_갱신(Long 서버_c_id, JiraProjectEntity jiraProjectEntity, String 설정할_항목) throws Exception {
        JiraProjectEntity 검색용_프로젝트_엔티티 = new JiraProjectEntity();
        검색용_프로젝트_엔티티.setC_id(jiraProjectEntity.getC_id());
        JiraProjectEntity 검색된_프로젝트_엔티티 = this.getNode(검색용_프로젝트_엔티티);

        JiraServerPureEntity 검색용_지라서버_엔티티 = new JiraServerPureEntity();
        검색용_지라서버_엔티티.setC_id(서버_c_id);
        String 연결_아이디 = jiraServerPure.getNode(검색용_지라서버_엔티티).getC_jira_server_etc();

        if(StringUtils.equals(설정할_항목,"issueType")) {
            Set<JiraIssueTypeEntity> 프로젝트_이슈유형_세트 = 검색된_프로젝트_엔티티.getJiraIssueTypeEntities();
            List<지라이슈유형_데이터> 클라우드_프로젝트별_이슈_유형_목록 = 엔진통신기.클라우드_프로젝트별_이슈_유형_목록(연결_아이디, 검색된_프로젝트_엔티티.getC_desc());

            for(지라이슈유형_데이터 가져온_이슈_유형 : 클라우드_프로젝트별_이슈_유형_목록) {
                if (기등록_이슈_유형_갱신결과(프로젝트_이슈유형_세트, 가져온_이슈_유형) == 0) {
                    프로젝트_이슈유형_세트.add(미등록_이슈_유형_저장_및_저장된_엔티티(가져온_이슈_유형));
                }
            }
            this.updateNode(검색된_프로젝트_엔티티);
        }
        if(StringUtils.equals(설정할_항목,"issueStatus")) {
            Set<JiraIssueStatusEntity> 프로젝트_이슈상태_세트 = 검색된_프로젝트_엔티티.getJiraIssueStatusEntities();
            List<지라이슈상태_데이터> 클라우드_프로젝트별_이슈_상태_목록 = 엔진통신기.클라우드_프로젝트별_이슈_상태_목록(연결_아이디, 검색된_프로젝트_엔티티.getC_desc());

            for(지라이슈상태_데이터 가져온_이슈_상태 : 클라우드_프로젝트별_이슈_상태_목록) {
                if (기등록_이슈_상태_갱신결과(프로젝트_이슈상태_세트, 가져온_이슈_상태) == 0) {
                    프로젝트_이슈상태_세트.add(미등록_이슈_상태_저장_및_저장된_엔티티(가져온_이슈_상태));
                }
            }
            this.updateNode(검색된_프로젝트_엔티티);
        }

        return 검색된_프로젝트_엔티티;
    }

    private int 기등록_이슈_상태_갱신결과(Set<JiraIssueStatusEntity> 기존_이슈_상태_목록, 지라이슈상태_데이터 가져온_이슈_상태) throws Exception {
        int 갱신_횟수 = 0;
        for(JiraIssueStatusEntity issueStatusEntity : 기존_이슈_상태_목록) {
            if (issueStatusEntity.getC_issue_status_url().equals(가져온_이슈_상태.getSelf())) {
                issueStatusEntity.setC_issue_status_name(가져온_이슈_상태.getName());
                issueStatusEntity.setC_issue_status_desc(가져온_이슈_상태.getDescription());
                갱신_횟수 += jiraIssueStatus.updateNode(issueStatusEntity);
            }
        }
        return 갱신_횟수;
    }

    private JiraIssueStatusEntity 미등록_이슈_상태_저장_및_저장된_엔티티(지라이슈상태_데이터 이슈_상태) throws Exception {

        JiraIssueStatusEntity 저장할_이슈_상태 = new JiraIssueStatusEntity();
        //공통
        저장할_이슈_상태.setC_issue_status_id(이슈_상태.getId());
        저장할_이슈_상태.setC_issue_status_name(이슈_상태.getName());
        저장할_이슈_상태.setC_issue_status_url(이슈_상태.getSelf());
        저장할_이슈_상태.setC_issue_status_desc(이슈_상태.getDescription());
        저장할_이슈_상태.setC_check("false");
        저장할_이슈_상태.setRef(TreeConstant.First_Node_CID);
        저장할_이슈_상태.setC_type(TreeConstant.Leaf_Node_TYPE);

        JiraIssueStatusEntity 저장된_지라이슈상태 = jiraIssueStatus.addNode(저장할_이슈_상태);
        return 저장된_지라이슈상태;
    }

    private int 기등록_이슈_유형_갱신결과(Set<JiraIssueTypeEntity> 기존_이슈_유형_목록, 지라이슈유형_데이터 가져온_이슈_유형) throws Exception {
        int 갱신_횟수 = 0;
        for (JiraIssueTypeEntity issueTypeEntity : 기존_이슈_유형_목록) {
            if (issueTypeEntity.getC_issue_type_url().equals(가져온_이슈_유형.getSelf())) {
                if (가져온_이슈_유형.getName() != null) {
                    issueTypeEntity.setC_issue_type_name(가져온_이슈_유형.getName());
                }
                if (가져온_이슈_유형.getDescription() != null) {
                    issueTypeEntity.setC_issue_type_desc(가져온_이슈_유형.getDescription());
                }
                if (가져온_이슈_유형.getSubtask() != null) {
                    issueTypeEntity.setC_desc(가져온_이슈_유형.getSubtask().toString());
                }
                if (가져온_이슈_유형.getUntranslatedName() != null) {
                    issueTypeEntity.setC_etc(가져온_이슈_유형.getUntranslatedName());
                }
                if (가져온_이슈_유형.getHierarchyLevel() != null) {
                    issueTypeEntity.setC_contents(가져온_이슈_유형.getHierarchyLevel().toString());
                }

                갱신_횟수 += jiraIssueType.updateNode(issueTypeEntity);
            }
        }
        return 갱신_횟수;
    }

    private JiraIssueTypeEntity 미등록_이슈_유형_저장_및_저장된_엔티티(지라이슈유형_데이터 이슈_유형) throws Exception {
        JiraIssueTypeEntity 저장할_이슈_유형 = new JiraIssueTypeEntity();
        // 공통
        if (이슈_유형.getId() != null) {
            저장할_이슈_유형.setC_issue_type_id(이슈_유형.getId());
        }
        if (이슈_유형.getName() != null) {
            저장할_이슈_유형.setC_issue_type_name(이슈_유형.getName());
        }
        if (이슈_유형.getSelf() != null) {
            저장할_이슈_유형.setC_issue_type_url(이슈_유형.getSelf());
        }
        if (이슈_유형.getDescription() != null) {
            저장할_이슈_유형.setC_issue_type_desc(이슈_유형.getDescription());
        }
        if (이슈_유형.getSubtask() != null) {
            저장할_이슈_유형.setC_desc(이슈_유형.getSubtask().toString()); //Boolean
        }
        if (이슈_유형.getName().equals("arms-requirement")) {
            저장할_이슈_유형.setC_check("true"); //기본값 false 설정
        } else {
            저장할_이슈_유형.setC_check("false"); //기본값 false 설정
        }
        저장할_이슈_유형.setRef(TreeConstant.First_Node_CID);
        저장할_이슈_유형.setC_type(TreeConstant.Leaf_Node_TYPE);
        if (이슈_유형.getUntranslatedName() != null) {
            저장할_이슈_유형.setC_etc(이슈_유형.getUntranslatedName());
        }
        if (이슈_유형.getHierarchyLevel() != null) {
            저장할_이슈_유형.setC_contents(이슈_유형.getHierarchyLevel().toString()); //Integer
        }

        JiraIssueTypeEntity 저장된_이슈_유형 = jiraIssueType.addNode(저장할_이슈_유형);

        return 저장된_이슈_유형;
    }
}