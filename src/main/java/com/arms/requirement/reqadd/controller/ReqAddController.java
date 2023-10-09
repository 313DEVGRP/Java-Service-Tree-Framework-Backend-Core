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
package com.arms.requirement.reqadd.controller;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import com.arms.requirement.reqadd.excelupload.ExcelGantUpload;
import com.arms.requirement.reqadd.excelupload.WbsSchedule;
import com.arms.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.util.filerepository.model.FileRepositoryDTO;
import com.arms.util.filerepository.model.FileRepositoryEntity;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.requirement.reqadd.model.ReqAddDTO;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.arms.requirement.reqadd.service.ReqAdd;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.excel.ExcelUtilsFactory;
import com.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import com.egovframework.javaservice.treeframework.validation.group.MoveNode;
import com.egovframework.javaservice.treeframework.validation.group.UpdateNode;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        reqAddEntity.setOrder(Order.asc("c_position"));
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

        log.info("ReqAddController :: getSwitchDBChildNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNode",changeReqTableName);

            reqAddEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
            reqAddEntity.setOrder(Order.asc("c_position"));
            List<ReqAddEntity> list = reqAdd.getChildNode(reqAddEntity);

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
                                               ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBChildNodeWithParent");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);
        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getChildNodeWithParent",changeReqTableName);

            Long targetId = new Long(parser.get("c_id"));
            Criterion criterion1 = Restrictions.eq("c_parentid", targetId);
            Criterion criterion2 = Restrictions.eq("c_id", targetId);
            Criterion criterion3 = Restrictions.or(criterion1, criterion2);
            reqAddEntity.getCriterions().add(criterion3);
            reqAddEntity.setOrder(Order.asc("c_position"));

            List<ReqAddEntity> list = reqAdd.getChildNode(reqAddEntity);

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
            ,ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("ReqAddController :: getSwitchDBNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException();
        } else {

            SessionUtil.setAttribute("getNode",changeReqTableName);

            ReqAddEntity returnVO = reqAdd.getNode(reqAddEntity);

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
            ,ReqAddDTO reqAddDTO, HttpServletRequest request) throws Exception {

        log.info("[ ReqAddController :: getSwitchDBNode ]");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("getReqAddListByFilter",changeReqTableName);

        String[] versionStrArr = StringUtils.split(reqAddEntity.getC_req_pdservice_versionset_link(), ",");

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
            reqAddEntity.getCriterions().add(orCondition);

            List<ReqAddEntity> savedList = reqAdd.getChildNode(reqAddEntity);

            SessionUtil.removeAttribute("getReqAddListByFilter");
            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", savedList);
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

        reqAddEntity.setC_req_create_date(new Date());
        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        pdServiceEntity.setC_id(reqAddDTO.getC_req_pdservice_link());
        PdServiceEntity savedPdService = pdService.getNode(pdServiceEntity);
        reqAddEntity.setPdServiceEntity(savedPdService);

        ReqAddEntity savedNode = reqAdd.addReqNode(reqAddEntity, changeReqTableName);

        log.info("ReqAddController :: addReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedNode));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/updateNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> updateReqNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @Validated({UpdateNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddController :: updateReqNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("updateNode",changeReqTableName);

        int savedReqAddEntity = reqAdd.updateNode(reqAddEntity);

        SessionUtil.removeAttribute("updateNode");

        log.info("ReqAddController :: updateReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedReqAddEntity));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/moveNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> moveReqNode(
            @PathVariable(value ="changeReqTableName") String changeReqTableName,
            @Validated({MoveNode.class}) ReqAddDTO reqAddDTO, HttpServletRequest request,
            BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("ReqAddController :: moveReqNode");
        ReqAddEntity reqAddEntity = modelMapper.map(reqAddDTO, ReqAddEntity.class);

        SessionUtil.setAttribute("moveNode",changeReqTableName);

        ReqAddEntity savedReqAddEntity = reqAdd.moveNode(reqAddEntity, request);

        SessionUtil.removeAttribute("moveNode");

        log.info("ReqAddController :: moveReqNode");
        return ResponseEntity.ok(CommonResponse.success(savedReqAddEntity));

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
    @GetMapping(value = "/{changeReqTableName}/getDetail.do")
    public ResponseEntity<ReqAddDetailDTO> followReqLink(FollowReqLinkDTO followReqLinkDTO,@PathVariable(value ="changeReqTableName") String changeReqTableName
        ) throws Exception {

        return  ResponseEntity.ok(reqAdd.getDetail(followReqLinkDTO,changeReqTableName));
    }


}
