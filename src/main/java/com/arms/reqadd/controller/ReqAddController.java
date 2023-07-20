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
package com.arms.reqadd.controller;

import com.arms.pdservice.model.PdServiceEntity;
import com.arms.pdservice.service.PdService;
import com.arms.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.pdsreqjiraissuelog.model.PdsReqJiraIssueLogEntity;
import com.arms.reqadd.model.ReqAddDTO;
import com.arms.reqadd.model.ReqAddEntity;
import com.arms.reqadd.service.ReqAdd;
import com.arms.reqpriority.model.ReqPriorityEntity;
import com.arms.reqpriority.service.ReqPriority;
import com.arms.reqstate.model.ReqStateEntity;
import com.arms.reqstate.service.ReqState;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Order;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqAdd"})
public class ReqAddController extends TreeAbstractController<ReqAdd, ReqAddDTO, ReqAddEntity> {

    @Autowired
    @Qualifier("reqAdd")
    private ReqAdd reqAdd;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(reqAdd);
        setTreeEntity(ReqAddEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getMonitor(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqAddDTO reqAddDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getMonitor");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("getMonitor",changeReqTableName);

        reqAddEntity.setOrder(Order.asc("c_left"));
        List<ReqAddEntity> list = this.reqAdd.getChildNode(reqAddEntity);

        SessionUtil.removeAttribute("getMonitor");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getChildNode.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBChildNode(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                             ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getMonitor");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNode",changeReqTableName);

            reqAddEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
            List<ReqAddEntity> list = reqAdd.getChildNode(reqAddEntity);

            SessionUtil.removeAttribute("getChildNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", list);
            return modelAndView;
        }
    }

    @Autowired
    @Qualifier("reqPriority")
    private ReqPriority reqPriority;

    @Autowired
    @Qualifier("reqState")
    private ReqState reqState;

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getNode.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName
            ,ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getNode",changeReqTableName);

            ReqAddEntity returnVO = reqAdd.getNode(reqAddEntity);

            ReqPriorityEntity reqPriorityEntity = new ReqPriorityEntity();
            reqPriorityEntity.setC_id(3L);
            ReqPriorityEntity savedObj = reqPriority.getNode(reqPriorityEntity);

            returnVO.setReqPriorityEntity(savedObj);
            SessionUtil.removeAttribute("getNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", returnVO);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/addNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addReqNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @Validated({AddNode.class}) ReqAddDTO reqAddDTO,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddController :: addReqNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(reqAddDTO.getC_req_pdservice_link());
        PdServiceEntity savedPdService = pdService.getNode(pdServiceEntity);
        reqAddEntity.setPdServiceEntity(savedPdService);

        ReqPriorityEntity reqPriorityEntity = new ReqPriorityEntity();
        reqPriorityEntity.setC_id(3L);
        ReqPriorityEntity priorityEntity = reqPriority.getNode(reqPriorityEntity);
        reqAddEntity.setReqPriorityEntity(priorityEntity);

        ReqStateEntity reqStateEntity = new ReqStateEntity();
        reqStateEntity.setC_id(3L);
        ReqStateEntity stateEntity = reqState.getNode(reqStateEntity);
        reqAddEntity.setReqStateEntity(stateEntity);


        ReqAddEntity savedNode = reqAdd.addReqNode(reqAddEntity, changeReqTableName);

        log.info("ReqAddController :: addReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedNode));

    }

}
