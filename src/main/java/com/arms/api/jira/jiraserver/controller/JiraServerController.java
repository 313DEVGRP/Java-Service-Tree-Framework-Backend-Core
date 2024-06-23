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
package com.arms.api.jira.jiraserver.controller;

import com.arms.api.jira.jiraserver.model.JiraServerDTO;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.EntityType;
import com.arms.api.jira.jiraserver.model.계정정보_데이터;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.util.communicate.external.request.지라서버정보_데이터;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.validation.group.AddNode;
import com.arms.egovframework.javaservice.treeframework.validation.group.UpdateNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/jiraServer"})
public class JiraServerController extends TreeAbstractController<JiraServer, JiraServerDTO, JiraServerEntity> {

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;

    @Autowired
    private 엔진통신기 엔진통신기;

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
    public ResponseEntity<?> ALM_서버_항목별_갱신(@PathVariable String renewTarget,
                                          String projectCId,
                                          JiraServerDTO jiraServerDTO) throws Exception{

        log.info("JiraServerController :: ALM_서버_항목별_갱신 갱신종류=> {}", renewTarget);
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        EntityType 갱신항목 = EntityType.fromString(renewTarget);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_엔티티_항목별_갱신(갱신항목, projectCId, jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraProject.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraprojectList(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraprojectList");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_프로젝트_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraProjectPure.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraprojectPure(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraprojectPure");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_프로젝트만_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueType.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueTypeList(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraIssueTypeList");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈유형_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueStatus.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueStatusList(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraIssueStatusList");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈상태_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssuePriority.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssuePriorityList(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraIssuePriorityList");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈우선순위_가져오기(jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getJiraIssueResolution.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> getJiraIssueResolutionList(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: getJiraIssueResolutionList");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_이슈해결책_가져오기(jiraServerEntity)));
    }


    @ResponseBody
    @RequestMapping(
            value = {"/{defaultTarget}/makeDefault.do"},
            method = {RequestMethod.PUT}
    )
    public ResponseEntity<?> 온프레미스_항목별_기본값_설정(@PathVariable(name="defaultTarget") String 설정할_항목,
                                                      @RequestParam(name="targetCid") Long 항목_c_id,
                                                      JiraServerDTO jiraServerDTO) throws Exception{

        log.info("JiraServerController :: 온프레미스_항목별_기본값_설정, 설정할_항목 : {}, 항목_c_id : {}", 설정할_항목, 항목_c_id);
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        EntityType 갱신항목 = EntityType.fromString(설정할_항목);

        return ResponseEntity.ok(CommonResponse.success(jiraServer.서버_항목별_기본값_설정(갱신항목, 항목_c_id, jiraServerEntity)));
    }

    @ResponseBody
    @RequestMapping(value = "/updateNodeAndEngineServerInfoUpdate.do", method = RequestMethod.PUT)
    public ModelAndView 암스_및_엔진_서버정보수정(@Validated(value = UpdateNode.class) JiraServerDTO JiraServerDTO,
                                         BindingResult bindingResult, HttpServletRequest request, ModelMap model) throws Exception {

        log.info("JiraServerController :: 암스_및_엔진_서버정보수정");
        JiraServerEntity treeSearchEntity = modelMapper.map(JiraServerDTO, JiraServerEntity.class);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", jiraServer.암스_및_엔진_서버정보수정(treeSearchEntity));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/verifyAccount.do"},
            method={RequestMethod.GET}
    )
    public ResponseEntity<?> 계정정보_검증하기(지라서버정보_데이터 지라서버정보_데이터) throws Exception {

        log.info("JiraServerController :: 계정정보_검증하기");

        try{
            계정정보_데이터 검증결과 = 엔진통신기.계정정보_검증하기(지라서버정보_데이터).getBody();
            return ResponseEntity.ok(CommonResponse.success( 검증결과));
        }catch (Exception e){
            log.error("온프라미스 계정 정보 조회시 오류가 발생하였습니다." + e.getMessage());
            CommonResponse.ApiResult<?> errorResult = CommonResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/renewServer.do"},
            method={RequestMethod.PUT}
    )
    public ResponseEntity<?> ALM_서버_전체_항목_갱신(JiraServerDTO jiraServerDTO) throws Exception {

        log.info("JiraServerController :: ALM_서버_전체_항목_갱신");
        JiraServerEntity jiraServerEntity = modelMapper.map(jiraServerDTO, JiraServerEntity.class);

        return ResponseEntity.ok(jiraServer.ALM_서버_전체_항목_갱신(jiraServerEntity));
    }

}
