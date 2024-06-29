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
package com.arms.api.requirement.reqstatus.controller;

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.jira.jiraissuepriority.service.JiraIssuePriority;
import com.arms.api.jira.jiraissuestatus.service.JiraIssueStatus;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.jira.jiraserver_pure.model.JiraServerPureEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.communicate.external.AggregationService;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.external.response.jira.지라이슈;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqStatus"})
public class ReqStatusController extends TreeAbstractController<ReqStatus, ReqStatusDTO, ReqStatusEntity> {

    @Autowired
    @Qualifier("reqStatus")
    private ReqStatus reqStatus;

    @Autowired
    @Qualifier("jiraServer")
    private JiraServer jiraServer;

    @Autowired
    @Qualifier("jiraIssuePriority")
    private JiraIssuePriority jiraIssuePriority;

    @Autowired
    @Qualifier("jiraIssueStatus")
    private JiraIssueStatus jiraIssueStatus;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private EngineService engineService;

    @PostConstruct
    public void initialize() {
        setTreeService(reqStatus);
        setTreeEntity(ReqStatusEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/addStatusNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> 요구사항_이슈_저장하기(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @RequestBody ReqStatusDTO reqStatusDTO) throws Exception {

        log.info("ReqStatusController :: addStatusNode");
        ReqStatusEntity reqStatusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("addStatusNode",changeReqTableName);

        ReqStatusEntity savedNode = reqStatus.addNode(reqStatusEntity);

        SessionUtil.removeAttribute("addStatusNode");

        log.info("ReqStatusController :: addStatusNode");
        return ResponseEntity.ok(CommonResponse.success(savedNode));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getStatusMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getStatusMonitor(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getStatusMonitor");
        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("getStatusMonitor",changeReqTableName);

        statusEntity.setOrder(Order.asc("c_id"));

        List<ReqStatusEntity> list = reqStatus.getNodesWithoutRoot(statusEntity);

        SessionUtil.removeAttribute("getStatusMonitor");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/requirement-linkedissue.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 제품별_요구사항_연결이슈_조회(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: 제품별_요구사항_연결이슈_조회");
        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", "");
        Long 제품서비스_아이디 = Long.parseLong(pdServiceStr);

        AggregationRequestDTO 요청 = new AggregationRequestDTO();

        ParameterParser parser = new ParameterParser(request);
        String versionsString = parser.get("version");
        요청.setPdServiceVersionLinks(Arrays.stream(versionsString.split(",")).map(Long::parseLong).collect(Collectors.toList()));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", engineService.제품별_요구사항_연결이슈_조회(제품서비스_아이디, 요청));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateStatusNode.do"},
            method = {RequestMethod.PUT}
    )
    public ResponseEntity<?> 요구사항_이슈_수정하기(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @RequestBody ReqStatusDTO reqStatusDTO) throws Exception {

        log.info("ReqStatusController :: updateStatusNode");
        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("updateStatusNode",changeReqTableName);

        int 결과 = reqStatus.updateNode(statusEntity);

        SessionUtil.removeAttribute("updateStatusNode");

        return ResponseEntity.ok(CommonResponse.success(결과));
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getStatistics.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getStatistics(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getStatistics");
        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("getStatistics",changeReqTableName);

        statusEntity.setOrder(Order.asc("c_left"));

        List<ReqStatusEntity> list = reqStatus.getNodesWithoutRoot(statusEntity);

        List<String> versionList = list.stream()
                .map(ReqStatusEntity::getC_req_pdservice_versionset_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> jiraServerList = list.stream()
                .map(ReqStatusEntity::getC_jira_server_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> jiraProjectList = list.stream()
                .map(ReqStatusEntity::getC_jira_project_link)
                .distinct()
                .collect(Collectors.toList());

        List<Long> reqList = list.stream()
                .map(ReqStatusEntity::getC_req_link)
                .distinct()
                .collect(Collectors.toList());

        List<String> issueList = list.stream()
                .map(ReqStatusEntity::getC_issue_key)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put("version", versionList.size());
        result.put("jiraServer", jiraServerList.size());
        result.put("jiraProject", jiraProjectList.size());
        result.put("req", reqList.size());
        result.put("issue", issueList.size());

        SessionUtil.removeAttribute("getStatistics");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }

    static final long dummy_jira_server = 0L;
    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getProgress.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getProgress(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", "");
        Long pdService = Long.parseLong(pdServiceStr);

        ParameterParser parser = new ParameterParser(request);
        String pds_version = parser.get("version");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result",
                        aggregationService.제품서비스_버전별_상태값_통계(pdService, Arrays.stream(pds_version.split(",")).map(Long::valueOf).toArray(Long[]::new)));
        return modelAndView;

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getIssueAndSubLinks.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getLinkedIssueAndSubtask(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getLinkedIssueAndSubtask");

        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);
        statusEntity.setOrder(Order.asc("c_left"));

        String pdServiceStr = StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", "");

        String 서버_아이디 = request.getParameter("serverId");
        JiraServerPureEntity 검색용_지라서버 = new JiraServerPureEntity();
        검색용_지라서버.setC_id(Long.parseLong(서버_아이디));
        JiraServerPureEntity 검색결과 = jiraServer.getNode(검색용_지라서버);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        if (검색결과 != null) {
            String 엔진통신_아이디 = 검색결과.getC_jira_server_etc();
            String 이슈키 = request.getParameter("issueKey");

            List<지라이슈> 링크드이슈_서브데스크 = engineService.지라_연결된이슈_서브테스크_가져오기(Long.parseLong(엔진통신_아이디), 이슈키, 0, 10);

            log.info("[ ReqStatusEntity :: getLinkedIssueAndSubtask ] :: 링크드이슈_서브데스크 = {}", 링크드이슈_서브데스크);

            modelAndView.addObject("result", 링크드이슈_서브데스크);
        } else {
            modelAndView.addObject("result", "");
            log.info("[ ReqStatusEntity :: getLinkedIssueAndSubtask ] :: 검색된 지라서버가 없습니다.");
        }

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getReqAndReqIssueAndLinkedIssue.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getReqAndReqIssueAndLinkedIssue(@PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, HttpServletRequest request) throws Exception {

        ReqStatusEntity statusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        statusEntity.setOrder(Order.asc("c_req_link")); // 요구사항 c_id

        ReqStatusEntity 검색_전용 = new ReqStatusEntity();
        검색_전용.setC_pdservice_link(statusEntity.getC_pdservice_link());
        검색_전용.setWhereIn("c_pds_version_link",request.getParameterValues("pdServiceLinks"));
        검색_전용.setOrder(Order.asc("c_req_link"));
        List<ReqStatusEntity> 요구사항_연결이슈_검색_결과 = reqStatus.getChildNode(검색_전용);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        return modelAndView;

    }


    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getPdReqStats.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getPdReqStats(@PathVariable(value ="changeReqTableName") String changeReqTableName, HttpServletRequest request) {

        log.info("ReqStatusController :: getPdReqStats");

        Long 제품서비스_아이디 = Long.parseLong(StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", ""));
        String 담당자_이메일 = request.getParameter("assigneeEmail");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", engineService.제품서비스별_담당자_요구사항_통계(dummy_jira_server, 제품서비스_아이디, 담당자_이메일));

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getPdRelatedReqStats.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getPdRelatedReqStats(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                     ReqStatusDTO reqStatusDTO, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getPdRelatedReqStats");

        Long 제품서비스_아이디 = Long.parseLong(StringUtils.replace(changeReqTableName, "T_ARMS_REQSTATUS_", ""));

        JiraServerPureEntity 검색용_지라서버 = new JiraServerPureEntity();
        검색용_지라서버.setC_id(reqStatusDTO.getC_jira_server_link());
        JiraServerPureEntity 검색결과_지라서버 = jiraServer.getNode(검색용_지라서버);

        ReqStatusEntity reqStatusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);
        SessionUtil.setAttribute("getPdRelatedReqStats", changeReqTableName);

        Criterion searchService = Restrictions.eq("c_pdservice_link", 제품서비스_아이디);
        Criterion searchReq = Restrictions.eq("c_req_link", reqStatusDTO.getC_req_link());
        Criterion criterion = Restrictions.and(searchService, searchReq);
        reqStatusEntity.getCriterions().add(criterion);
        ReqStatusEntity 검색결과_요구사항 = reqStatus.getNode(reqStatusEntity);

        Long 지라서버_아이디 = Long.parseLong(검색결과_지라서버.getC_jira_server_etc());
        String 이슈키 = 검색결과_요구사항.getC_issue_key();
        String 담당자_이메일 = request.getParameter("assigneeEmail");

        log.info("지라서버_아이디: " + 지라서버_아이디);
        log.info("제품서비스_아이디: " + 제품서비스_아이디);
        log.info("이슈키: " + 이슈키);
        log.info("담당자_이메일: " + 담당자_이메일);

        SessionUtil.removeAttribute("getPdRelatedReqStats");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", engineService.제품서비스별_담당자_연관된_요구사항_통계(지라서버_아이디, 제품서비스_아이디, 이슈키, 담당자_이메일));

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getReqStatusListByCReqLink.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<List<ReqStatusEntity>> getReqStatusListByCReqLink(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqStatusDTO reqStatusDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqStatusController :: getReqStatusListByCReqLink");
        ReqStatusEntity reqStatusEntity = modelMapper.map(reqStatusDTO, ReqStatusEntity.class);

        SessionUtil.setAttribute("getReqStatusListByCReqLink", changeReqTableName);

        Long cReqLink = reqStatusEntity.getC_req_link();
        reqStatusEntity.getCriterions().add(Restrictions.eq("c_req_link", cReqLink));
        reqStatusEntity.setOrder(Order.asc("c_id"));

        List<ReqStatusEntity> list = reqStatus.getChildNode(reqStatusEntity);

        SessionUtil.removeAttribute("getReqStatusListByCReqLink");

        return ResponseEntity.ok(list);
    }
}
