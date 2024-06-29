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
package com.arms.api.product_service.pdservicelog.controller;

import com.arms.api.product_service.pdservicelog.model.PdServiceLogDTO;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.product_service.pdservicelog.model.PdServiceLogEntity;
import com.arms.api.product_service.pdservicelog.service.PdServiceLog;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/arms/pdServiceLog"})
public class PdServiceLogController extends TreeAbstractController<PdServiceLog, PdServiceLogDTO, PdServiceLogEntity> {

    private final PdServiceLog pdServiceLog;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServiceLog);
        setTreeEntity(PdServiceLogEntity.class);
    }

}
