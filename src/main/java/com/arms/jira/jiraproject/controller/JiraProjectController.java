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
package com.arms.jira.jiraproject.controller;

import com.arms.jira.jiraproject.model.JiraProjectDTO;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.requirement.reqadd.model.ReqAddDTO;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.jira.jiraproject.service.JiraProject;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraProject"})
public class JiraProjectController extends TreeAbstractController<JiraProject, JiraProjectDTO, JiraProjectEntity> {

    @Autowired
    @Qualifier("jiraProject")
    private JiraProject jiraProject;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraProject);
        setTreeEntity(JiraProjectEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ResponseBody
    @RequestMapping(value = "/getConnectionInfo.do", method = RequestMethod.GET)
    public ResponseEntity<?> getConnectionInfo(ReqAddDTO reqAddDTO) throws Exception {

        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);
        System.out.println("ReqAddController :: getConnectionInfo");


        List<JiraProjectEntity> connectionInfo = jiraProject.getConnectionInfo(reqAddEntity);
        logger.info("connectionInfo::" + connectionInfo);
        return ResponseEntity.ok(CommonResponse.success(connectionInfo));
    }

}
