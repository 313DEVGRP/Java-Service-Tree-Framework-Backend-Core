/*
 * @author Dongmin.lee
 * @since 2023-03-28
 * @version 23.03.28
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.jira.jiraserver_project_pure.controller;

import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureDTO;
import com.arms.api.jira.jiraserver_project_pure.model.JiraServerProjectPureEntity;
import com.arms.api.jira.jiraserver_project_pure.service.JiraServerProjectPure;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Order;
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
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraServerProjectPure"})
public class JiraServerProjectPureController extends TreeAbstractController<JiraServerProjectPure, JiraServerProjectPureDTO, JiraServerProjectPureEntity> {

    @Autowired
    @Qualifier("jiraServerProjectPure")
    private JiraServerProjectPure jiraServerProjectPure;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraServerProjectPure);
        setTreeEntity(JiraServerProjectPureEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value= { "/getJiraServerMonitor.do"},
            method= {RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraServerMonitor(JiraServerProjectPureDTO jiraServerProjectPureDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("JiraServerProjectPureController :: getJiraServerMonitor");
        JiraServerProjectPureEntity jiraServerProjectPureEntity = modelMapper.map(jiraServerProjectPureDTO, JiraServerProjectPureEntity.class);

        return ResponseEntity.ok(jiraServerProjectPure.getNodesWithoutRoot(jiraServerProjectPureEntity));
    }

    @ResponseBody
    @RequestMapping(value = "/getChildNodeWithoutSoftDelete.do", method = RequestMethod.GET)
    public ModelAndView getChildNodeWithoutSoftDelete(JiraServerProjectPureDTO jiraServerProjectPureDTO, HttpServletRequest request)
            throws Exception {

        log.info("JiraServerProjectPureController :: getChildNodeWithoutSoftDelete");
        JiraServerProjectPureEntity jiraServerProjectPureEntity
                = modelMapper.map(jiraServerProjectPureDTO, JiraServerProjectPureEntity.class);

        ParameterParser parser = new ParameterParser(request);

         if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException("c_id is minus value");
        }

        jiraServerProjectPureEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
        jiraServerProjectPureEntity.setOrder(Order.desc("c_position"));
        List<JiraServerProjectPureEntity> list = jiraServerProjectPure.getChildNodeWithoutSoftDelete(jiraServerProjectPureEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getNodeWithoutSoftDelete.do", method = RequestMethod.GET)
    public ResponseEntity<?> getNodeWithoutSoftDelete(JiraServerProjectPureDTO jiraServerProjectPureDTO, HttpServletRequest request)
            throws Exception {

        log.info("JiraServerProjectPureController :: getNodeWithoutSoftDelete");
        JiraServerProjectPureEntity jiraServerProjectPureEntity
                = modelMapper.map(jiraServerProjectPureDTO, JiraServerProjectPureEntity.class);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException("c_id is minus value");
        }

        JiraServerProjectPureEntity result = jiraServerProjectPure.getNodeWithoutSoftDelete(jiraServerProjectPureEntity);

        return ResponseEntity.ok(result);
    }
}
