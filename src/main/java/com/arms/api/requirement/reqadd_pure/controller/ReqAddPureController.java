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
package com.arms.api.requirement.reqadd_pure.controller;

import com.arms.api.requirement.reqadd.excelupload.ExcelGantUpload;
import com.arms.api.requirement.reqadd.excelupload.WbsSchedule;
import com.arms.api.requirement.reqadd_pure.model.ReqAddPureDTO;
import com.arms.api.requirement.reqadd_pure.model.ReqAddPureEntity;
import com.arms.api.requirement.reqadd_pure.service.ReqAddPure;
import com.arms.api.util.filerepository.model.FileRepositoryDTO;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.arms.egovframework.javaservice.treeframework.validation.group.MoveNode;
import com.arms.egovframework.javaservice.treeframework.validation.group.UpdateNode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqAddPure"})
public class ReqAddPureController extends TreeAbstractController<ReqAddPure, ReqAddPureDTO, ReqAddPureEntity> {

    @Autowired
    @Qualifier("reqAddPure")
    private ReqAddPure reqAddPure;

    @PostConstruct
    public void initialize() {
        setTreeService(reqAddPure);
        setTreeEntity(ReqAddPureEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getMonitor(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            ReqAddPureDTO reqAddPureDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqAddPureController :: getMonitor");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        SessionUtil.setAttribute("getMonitor",changeReqTableName);

        reqAddPureEntity.setOrder(Order.asc("c_position"));
        List<ReqAddPureEntity> list = reqAddPure.getChildNodeWithoutPaging(reqAddPureEntity);

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
                                             ReqAddPureDTO reqAddPureDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddPureController :: getSwitchDBChildNode");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNode",changeReqTableName);

            reqAddPureEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
            reqAddPureEntity.setOrder(Order.asc("c_position"));
            List<ReqAddPureEntity> list = reqAddPure.getChildNode(reqAddPureEntity);

            SessionUtil.removeAttribute("getChildNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", list);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getChildNodeWithParent.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBChildNodeWithParent(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                               ReqAddPureDTO reqAddPureDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddPureController :: getSwitchDBChildNodeWithParent");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNodeWithParent",changeReqTableName);

            Long targetId = new Long(parser.get("c_id"));
            Criterion criterion1 = Restrictions.eq("c_parentid", targetId);
            Criterion criterion2 = Restrictions.eq("c_id", targetId);
            Criterion criterion3 = Restrictions.or(criterion1, criterion2);
            reqAddPureEntity.getCriterions().add(criterion3);
            reqAddPureEntity.setOrder(Order.asc("c_position"));

            List<ReqAddPureEntity> list = reqAddPure.getChildNode(reqAddPureEntity);

            SessionUtil.removeAttribute("getChildNodeWithParent");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", list);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getNode.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getSwitchDBNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName
            ,ReqAddPureDTO reqAddPureDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddPureController :: getSwitchDBNode");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getNode",changeReqTableName);

            ReqAddPureEntity returnVO = reqAddPure.getNode(reqAddPureEntity);

            SessionUtil.removeAttribute("getNode");

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", returnVO);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/getReqAddListByFilter.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getReqAddListByFilter(
            @PathVariable(value ="changeReqTableName") String changeReqTableName
            ,ReqAddPureDTO reqAddPureDTO, HttpServletRequest request) throws Exception {

        log.info("[ ReqAddPureController :: getSwitchDBNode ]");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        SessionUtil.setAttribute("getReqAddListByFilter",changeReqTableName);

        String[] versionStrArr = StringUtils.split(reqAddPureEntity.getC_req_pdservice_versionset_link(), ",");

        if ( versionStrArr == null || versionStrArr.length == 0){
            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", "result is empty");
            return modelAndView;
        }else{
            Disjunction orCondition = Restrictions.disjunction();
            for ( String versionStr : versionStrArr ){
                versionStr = "\\\"" + versionStr + "\\\"";
                orCondition.add(Restrictions.like("c_req_pdservice_versionset_link", versionStr, MatchMode.ANYWHERE));
            }
            reqAddPureEntity.getCriterions().add(orCondition);

            List<ReqAddPureEntity> savedList = reqAddPure.getChildNode(reqAddPureEntity);

            SessionUtil.removeAttribute("getReqAddListByFilter");
            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", savedList);
            return modelAndView;
        }

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/removeNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> removeReqNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddPureDTO reqAddPureDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddPureController :: removeNode");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        SessionUtil.setAttribute("removeNode",changeReqTableName);

        int removedReqAddPureEntity = reqAddPure.removeNode(reqAddPureEntity);

        SessionUtil.removeAttribute("removeNode");

        log.info("ReqAddPureController :: removeNode");
        return ResponseEntity.ok(CommonResponse.success(removedReqAddPureEntity));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/moveNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> moveReqNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @Validated({MoveNode.class}) ReqAddPureDTO reqAddPureDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddPureController :: moveReqNode");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);

        SessionUtil.setAttribute("moveNode",changeReqTableName);

        ReqAddPureEntity savedReqAddPureEntity = reqAddPure.moveNode(reqAddPureEntity, request);

        SessionUtil.removeAttribute("moveNode");

        log.info("ReqAddPureController :: moveReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedReqAddPureEntity));

    }

    @ResponseBody
    @RequestMapping(value="/uploadFileToNode.do", method = RequestMethod.POST)
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest,
                                         HttpServletRequest request, Model model) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        HashMap<String, Set<FileRepositoryEntity>> map = new HashMap();

        Set<FileRepositoryEntity> entitySet = Collections.emptySet();
        map.put("files", entitySet);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getFilesByNode.do", method = RequestMethod.GET)
    public ModelAndView getFilesByNode(FileRepositoryDTO fileRepositoryDTO, HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        HashMap<String, Set<FileRepositoryEntity>> returnMap = new HashMap();

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", returnMap);

        return modelAndView;
    }

    @ResponseBody
    @PostMapping(value = "/sample/excel-to-list")
    public ResponseEntity excelUpload(@RequestPart("excelFile") MultipartFile excelFile, HttpServletRequest request) throws Exception {
        //확인후에 저장을 하기 위한 샘플입니다.
        return ResponseEntity.ok(CommonResponse.success(
            new ExcelGantUpload(excelFile.getInputStream())
                .getGetWebScheduleList()
                .stream()
                .sorted(comparing(WbsSchedule::getDepth).reversed())
                .collect(toList())
            )
        );
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/reqProgress.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView reqProgress(@PathVariable(value ="changeReqTableName") String changeReqTableName,
            @RequestParam(required = false) Long pdServiceId,
            @RequestParam(required = false) List<Long> pdServiceVersionLinks,
            ReqAddPureDTO reqAddPureDTO, ModelMap model, HttpServletRequest request) throws Exception {

        long 시작시간 = System.currentTimeMillis();

        log.info("ReqAddPureController :: reqProgress");
        ReqAddPureEntity reqAddPureEntity = modelMapper.map(reqAddPureDTO, ReqAddPureEntity.class);
        List<ReqAddPureEntity> list = reqAddPure.reqProgress(reqAddPureEntity, changeReqTableName, pdServiceId, pdServiceVersionLinks, request);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);

        long 종료시간 = System.currentTimeMillis();

        long 걸린시간 = 종료시간 - 시작시간;
        log.info("API 호출이 걸린 시간: " + 걸린시간 + "밀리초");

        return modelAndView;
    }
}