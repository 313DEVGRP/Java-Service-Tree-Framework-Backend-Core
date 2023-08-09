/*
 * @author Dongmin.lee
 * @since 2023-05-05
 * @version 23.05.05
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.requirement.reqprioritylog.controller;

import com.arms.requirement.reqprioritylog.model.ReqPriorityLogDTO;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.requirement.reqprioritylog.model.ReqPriorityLogEntity;
import com.arms.requirement.reqprioritylog.service.ReqPriorityLog;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqPriorityLog"})
public class ReqPriorityLogController extends TreeAbstractController<ReqPriorityLog, ReqPriorityLogDTO, ReqPriorityLogEntity> {

    @Autowired
    @Qualifier("reqPriorityLog")
    private ReqPriorityLog reqPriorityLog;

    @PostConstruct
    public void initialize() {
        setTreeService(reqPriorityLog);
        setTreeEntity(ReqPriorityLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
