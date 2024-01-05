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
package com.arms.api.requirement.reqdifficulty.controller;


import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyDTO;
import com.arms.api.requirement.reqdifficulty.service.ReqDifficulty;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqDifficulty"})
public class ReqDifficultyController extends TreeAbstractController<ReqDifficulty, ReqDifficultyDTO, ReqDifficultyEntity> {

    @Autowired
    @Qualifier("reqDifficulty")
    private ReqDifficulty reqDifficulty;

    @PostConstruct
    public void initialize() {
        setTreeService(reqDifficulty);
		setTreeEntity(ReqDifficultyEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
