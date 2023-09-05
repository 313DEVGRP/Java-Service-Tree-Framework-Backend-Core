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
package com.arms.jira.jiraserver.controller;

import com.arms.jira.jiraserver.model.JiraServerDTO;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.arms.jira.jiraserver.model.JiraServerEntity;
import com.arms.jira.jiraserver.service.JiraServer;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraServer"})
public class JiraServerController extends TreeAbstractController<JiraServer, JiraServerDTO, JiraServerEntity> {

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;

    @PostConstruct
    public void initialize() {
        setTreeService(jiraServer);
        setTreeEntity(JiraServerEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = { "/addJiraServerNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addJiraServerNode(@Validated({AddNode.class}) JiraServerDTO jiraServerDTO,
                                            BindingResult bindingResult, ModelMap model) throws  Exception {

        log.info("JiraServerController :: addJiraServerNode");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        chat.sendMessageByEngine("지라(서버)가 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success(jiraServer.addJiraServer(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value= { "/getJiraServerMonitor.do"},
            method= {RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraServerMonitor(JiraServerDTO jiraServerDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("JiraServerController :: getJiraServerMonitor");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.getNodesWithoutRoot(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value={"{renewTarget}/renewNode.do"},
            method = {RequestMethod.PUT }
    )
    public ResponseEntity<?> 지라_서버_항목별_갱신(
            @PathVariable String renewTarget, JiraServerDTO jiraServerDTO) throws Exception{
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_엔티티_항목별_갱신(renewTarget, jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraproject.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraprojectList(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_프로젝트_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraprojectPure.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraprojectPure(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_프로젝트만_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueType.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueTypeList(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈유형_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueStatus.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueStatusList(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈상태_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssuePriority.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssuePriorityList(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈우선순위_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueResolution.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueResolutionList(JiraServerDTO jiraServerDTO) throws Exception {

        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈해결책_가져오기(jiraServerEntity)));
    }





    @ResponseBody
    @RequestMapping(
            value = {"/{defaultTarget}/makeDefault.do/{targetCid}"},
            method = {RequestMethod.PUT}
    )
    public ResponseEntity<?> 온프레미스_항목별_기본값_설정(@PathVariable(name="defaultTarget") String 설정할_항목,
                                                      @PathVariable(name="targetCid") Long 항목_c_id,
                                                      JiraServerDTO jiraServerDTO) throws Exception{
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_항목별_기본값_설정(설정할_항목,항목_c_id,jiraServerEntity)));
    }

}
