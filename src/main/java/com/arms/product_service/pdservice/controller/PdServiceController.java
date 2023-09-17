/*
 * @author Dongmin.lee
 * @since 2022-06-17
 * @version 22.06.17
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.product_service.pdservice.controller;

import com.arms.util.filerepository.model.FileRepositoryEntity;
import com.arms.product_service.pdservice.model.PdServiceDTO;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.product_service.pdservice.service.PdService;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.util.*;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Set;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = {"/arms/pdService"})
public class PdServiceController extends TreeAbstractController<PdService, PdServiceDTO, PdServiceEntity> {

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(pdService);
        setTreeEntity(PdServiceEntity.class);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/addPdServiceNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addPdServiceNode(@Validated({AddNode.class}) PdServiceDTO pdServiceDTO,
                                         BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("PdServiceController :: addPdServiceNode");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        chat.sendMessageByEngine("제품(서비스)가 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success(pdService.addPdServiceAndVersion(pdServiceEntity)));
    }

    @RequestMapping(
            value = {"/addVersionToNode.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addVersionToNode(@RequestBody PdServiceDTO pdServiceDTO,
                                              BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("PdServiceController :: addVersionToNode");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        chat.sendMessageByEngine("버전이 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success(pdService.addPdServiceVersion(pdServiceEntity)));
    }

    @RequestMapping(value="/updateVersionToNode.do", method= RequestMethod.PUT)
    public ModelAndView updateVersionNode(@RequestBody PdServiceVersionDTO pdServiceVersionDTO,
                                          BindingResult bindingResult, HttpServletRequest request) throws Exception {

        log.info("PdServiceVersionController :: updateVersionNode");
        PdServiceVersionEntity pdServiceVersionEntity = modelMapper.map(pdServiceVersionDTO, PdServiceVersionEntity.class);

        ModelAndView modelAndView = new ModelAndView("jsonView");

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        modelAndView.addObject("result", pdService.updatePdServiceVersion(pdservice_link, pdServiceVersionEntity));

        chat.sendMessageByEngine("제품(서비스) 버전이 수정되었습니다.");

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/addEndNodeByRoot.do", method = RequestMethod.POST)
    public ResponseEntity<?> addEndNodeByRoot(@RequestBody PdServiceDTO pdServiceDTO,
                                         BindingResult bindingResult) throws Exception {

        log.info("PdServiceController :: addEndNodeByRoot");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        return ResponseEntity.ok(CommonResponse.success(pdService.addNodeToEndPosition(pdServiceEntity)));
    }

    /**
     * 이미지 Upload를 처리한다.
     *
     * @param multiRequest
     * @param model
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/uploadFileToNode.do", method = RequestMethod.POST)
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest,
                                         HttpServletRequest request, Model model) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        HashMap<String, Set<FileRepositoryEntity>> map = new HashMap();

        map.put("files", pdService.uploadFileForPdServiceNode(pdservice_link, multiRequest));
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getPdServiceMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getPdServiceMonitor(PdServiceDTO pdServiceDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("PdServiceController :: getPdServiceMonitor");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        return ResponseEntity.ok(CommonResponse.success(pdService.getNodesWithoutRoot(pdServiceEntity)));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/getVersionList.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getVersionList(PdServiceDTO pdServiceDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("PdServiceController :: getVersionList");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        PdServiceEntity pdServiceNode = pdService.getNode(pdServiceEntity);
        Set<PdServiceVersionEntity> pdServiceVersionList = pdServiceNode.getPdServiceVersionEntities();

        return ResponseEntity.ok(CommonResponse.success(pdServiceVersionList));
    }

    @RequestMapping(value="/removeVersion.do", method= RequestMethod.DELETE)
    public ModelAndView removeVersion(HttpServletRequest request) throws Exception {

        log.info("PdServiceController :: removeVersion");

        ParameterParser parser = new ParameterParser(request);

        long pdServiceID = parser.getLong("pdservice_c_id");
        long versionID = parser.getLong("version_c_id");


        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdService.removeVersionNode(pdServiceID, versionID));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getD3ChartData.do", method = RequestMethod.GET)
    public ResponseEntity<?> getD3ChartData() throws Exception {
        return ResponseEntity.ok(CommonResponse.success(pdService.getD3ChartData()));
    }

}