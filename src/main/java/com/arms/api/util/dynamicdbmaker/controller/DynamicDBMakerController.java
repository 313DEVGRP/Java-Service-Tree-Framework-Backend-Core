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
package com.arms.api.util.dynamicdbmaker.controller;

import com.arms.api.util.dynamicdbmaker.model.DynamicDBMakerDTO;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

import com.arms.api.util.dynamicdbmaker.model.DynamicDBMakerEntity;
import com.arms.api.util.dynamicdbmaker.service.DynamicDBMaker;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/dynamicDBMaker"})
public class DynamicDBMakerController extends TreeAbstractController<DynamicDBMaker, DynamicDBMakerDTO, DynamicDBMakerEntity> {

    @Autowired
    @Qualifier("dynamicDBMaker")
    private DynamicDBMaker dynamicDBMaker;

    @PostConstruct
    public void initialize() {
        setTreeService(dynamicDBMaker);
        setTreeEntity(DynamicDBMakerEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @GetMapping("/createSchema")
    public ModelAndView createSchema() throws Exception {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", dynamicDBMaker.createSchema("313"));

        return modelAndView;
    }

}
