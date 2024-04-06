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
package com.arms.api.analysis.salary.controller;

import com.arms.api.analysis.salary.model.SalaryLogDTO;
import com.arms.api.analysis.salary.model.SalaryLogEntity;
import com.arms.api.analysis.salary.service.SalaryLog;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/salaryLog"})
public class SalaryLogController extends TreeAbstractController<SalaryLog, SalaryLogDTO, SalaryLogEntity> {

    @Autowired
    @Qualifier("salaryLog")
    private SalaryLog salaryLog;

    @PostConstruct
    public void initialize() {
        setTreeService(salaryLog);
        setTreeEntity(SalaryLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
