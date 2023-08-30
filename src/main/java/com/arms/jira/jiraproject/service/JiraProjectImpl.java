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
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
}