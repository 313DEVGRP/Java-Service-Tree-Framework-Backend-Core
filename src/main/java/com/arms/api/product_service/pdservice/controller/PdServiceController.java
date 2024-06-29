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
package com.arms.api.product_service.pdservice.controller;

import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.product_service.pdservice.model.PdServiceDTO;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.util.*;
import com.arms.egovframework.javaservice.treeframework.validation.group.AddNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/arms/pdService"})
public class PdServiceController extends TreeAbstractController<PdService, PdServiceDTO, PdServiceEntity> {

    private final PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(pdService);
        setTreeEntity(PdServiceEntity.class);
    }

    @GetMapping("/getNodeWithVersionOrderByCidDesc.do")
    public ModelAndView getNodeWithVersionOrderByCidDesc(PdServiceDTO pdServiceDTO) throws Exception {

        log.info("PdServiceController :: getNodeWithVersionOrderByCidDesc");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);
        PdServiceEntity pdServiceNode = pdService.getNodeWithVersionOrderByCidDesc(pdServiceEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceNode);
        return modelAndView;
    }

    @PostMapping("/addPdServiceNode.do")
    public ResponseEntity<?> addPdServiceNode(@Validated({AddNode.class}) PdServiceDTO pdServiceDTO) throws Exception {

        log.info("PdServiceController :: addPdServiceNode");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        chat.sendMessageByEngine("제품(서비스)가 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success(pdService.addPdServiceAndVersion(pdServiceEntity)));
    }

    @PostMapping("/addVersionToNode.do")
    public ResponseEntity<?> addVersionToNode(@RequestBody PdServiceDTO pdServiceDTO) throws Exception {

        log.info("PdServiceController :: addVersionToNode");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        chat.sendMessageByEngine("버전이 추가되었습니다.");

        return ResponseEntity.ok(CommonResponse.success(pdService.addPdServiceVersion(pdServiceEntity)));
    }

    @PutMapping("/updateVersionToNode.do")
    public ModelAndView updateVersionNode(@RequestBody PdServiceVersionDTO pdServiceVersionDTO, HttpServletRequest request) throws Exception {

        log.info("PdServiceVersionController :: updateVersionNode");
        PdServiceVersionEntity pdServiceVersionEntity = modelMapper.map(pdServiceVersionDTO, PdServiceVersionEntity.class);

        ModelAndView modelAndView = new ModelAndView("jsonView");

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        modelAndView.addObject("result", pdService.updatePdServiceVersion(pdservice_link, pdServiceVersionEntity));

        chat.sendMessageByEngine("제품(서비스) 버전이 수정되었습니다.");

        return modelAndView;
    }

    @PostMapping("/uploadFileToNode.do")
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest, HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        long pdservice_link = parser.getLong("pdservice_link");

        HashMap<String, Set<FileRepositoryEntity>> map = new HashMap();

        map.put("files", pdService.uploadFileForPdServiceNode(pdservice_link, multiRequest));
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);
        return modelAndView;
    }

    @GetMapping("/getPdServiceMonitor.do")
    public ResponseEntity<?> getPdServiceMonitor(PdServiceDTO pdServiceDTO) throws Exception {

        log.info("PdServiceController :: getPdServiceMonitor");
        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        return ResponseEntity.ok(CommonResponse.success(pdService.getNodesWithoutRoot(pdServiceEntity)));

    }

    @GetMapping("/getVersionList.do")
    public ResponseEntity<?> getVersionList(PdServiceDTO pdServiceDTO) throws Exception {
        log.info("PdServiceController :: getVersionList");

        PdServiceEntity pdServiceEntity = modelMapper.map(pdServiceDTO, PdServiceEntity.class);

        PdServiceEntity pdServiceNode = pdService.getNodeWithVersionOrderByCidDesc(pdServiceEntity);

        return ResponseEntity.ok(CommonResponse.success(pdServiceNode.getPdServiceVersionEntities()));
    }

    @DeleteMapping("/removeVersion.do")
    public ModelAndView removeVersion(HttpServletRequest request) throws Exception {

        log.info("PdServiceController :: removeVersion");

        ParameterParser parser = new ParameterParser(request);

        long pdServiceID = parser.getLong("pdservice_c_id");
        long versionID = parser.getLong("version_c_id");


        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdService.removeVersionNode(pdServiceID, versionID));
        return modelAndView;
    }

    @GetMapping("/getD3ChartData.do")
    public ResponseEntity<?> getD3ChartData() throws Exception {
        return ResponseEntity.ok(CommonResponse.success(pdService.getD3ChartData()));
    }

    @PostMapping("/removeAll.do/{pdServiceId}")
    public ResponseEntity<?> removeAll(@PathVariable("pdServiceId") Long pdServiceId) throws Exception {
        int result = pdService.removeAll(pdServiceId);
        return ResponseEntity.ok(CommonResponse.success(result));
    }

}