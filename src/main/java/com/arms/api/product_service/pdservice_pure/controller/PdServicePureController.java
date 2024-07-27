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
package com.arms.api.product_service.pdservice_pure.controller;

import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailEntity;
import com.arms.api.product_service.pdservice_detail.service.PdServiceDetail;
import com.arms.api.product_service.pdservice_pure.model.PdServicePureDTO;
import com.arms.api.product_service.pdservice_pure.model.PdServicePureEntity;
import com.arms.api.product_service.pdservice_pure.model.PdServiceWithDetailDTO;
import com.arms.api.product_service.pdservice_pure.service.PdServicePure;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/arms/pdServicePure"})
public class PdServicePureController extends TreeAbstractController<PdServicePure, PdServicePureDTO, PdServicePureEntity> {

    private final PdServicePure pdServicePure;
    private final PdServiceDetail pdServiceDetail;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServicePure);
        setTreeEntity(PdServicePureEntity.class);
    }

    @GetMapping("getPdServiceMonitor.do")
    public ResponseEntity<?> getPdServiceMonitor(PdServicePureDTO pdServicePureDTO) throws Exception {

        log.info("PdServiceController :: getPdServiceMonitor");
        PdServicePureEntity pdServicePureEntity = modelMapper.map(pdServicePureDTO, PdServicePureEntity.class);

        return ResponseEntity.ok(CommonResponse.success(pdServicePure.getNodesWithoutRoot(pdServicePureEntity)));

    }

    @GetMapping("getPdServiceWithDetail.do")
    public ResponseEntity<?> getPdServiceWithDetail(PdServicePureDTO pdServicePureDTO) throws Exception {

        log.info("PdServiceController :: getPdServiceWithDetail");

        PdServicePureEntity pureEntity = modelMapper.map(pdServicePureDTO, PdServicePureEntity.class);

        PdServicePureEntity pdServicePureEntity = pdServicePure.getNode(pureEntity);

        List<PdServiceDetailEntity> pdServiceDetailEntities = pdServiceDetail.getNodesByPdService(pdServicePureEntity.getC_id());

        PdServiceWithDetailDTO pdServiceWithDetailDTO = PdServiceWithDetailDTO.builder()
                .pdServicePure(pdServicePureEntity)
                .pdServiceDetails(pdServiceDetailEntities)
                .build();

        return ResponseEntity.ok(CommonResponse.success(pdServiceWithDetailDTO));

    }
}