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
package com.arms.product_service.pdservice_pure.controller;

import com.arms.product_service.pdservice.model.PdServiceDTO;
import com.arms.product_service.pdservice_pure.model.PdServicePureDTO;
import com.arms.product_service.pdservice_pure.model.PdServicePureEntity;
import com.arms.product_service.pdservice_pure.service.PdServicePure;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RestController
@AllArgsConstructor
@RequestMapping(value = {"/arms/pdServicePure"})
public class PdServicePureController extends TreeAbstractController<PdServicePure, PdServicePureDTO, PdServicePureEntity> {

    @Autowired
    @Qualifier("pdServicePure")
    private PdServicePure pdServicePure;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServicePure);
        setTreeEntity(PdServicePureEntity.class);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getPdServiceMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getPdServiceMonitor(PdServiceDTO pdServiceDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("PdServiceController :: getPdServiceMonitor");
        PdServicePureEntity pdServicePureEntity = modelMapper.map(pdServiceDTO, PdServicePureEntity.class);

        return ResponseEntity.ok(CommonResponse.success(pdServicePure.getNodesWithoutRoot(pdServicePureEntity)));

    }
}