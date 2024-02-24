/*
 * @author Dongmin.lee
 * @since 2023-10-22
 * @version 23.10.22
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqstate.controller;


import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.model.ReqStateDTO;
import com.arms.api.requirement.reqstate.service.ReqState;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqState"})
public class ReqStateController extends TreeAbstractController<ReqState, ReqStateDTO, ReqStateEntity> {

    @Autowired
    @Qualifier("reqState")
    private ReqState reqState;

    @PostConstruct
    public void initialize() {
        setTreeService(reqState);
		setTreeEntity(ReqStateEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${requirement.state.complete.keyword}")
    private String 완료_키워드;

    @ResponseBody
    @RequestMapping(
            value = {"/complete-keyword"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 요구사항_완료_키워드조회() throws Exception {
        logger.info(" [ " + this.getClass().getName() + " :: 요구사항_완료_키워드조회 ]");
        Set<String> 완료_키워드_셋 = new HashSet<>(Arrays.asList(완료_키워드.split(",")));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 완료_키워드_셋);
        return modelAndView;
    }
}
