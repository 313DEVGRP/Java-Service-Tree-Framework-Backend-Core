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
package com.arms.requirement.reqdifficulty.controller;


import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import com.egovframework.javaservice.treeframework.validation.group.AddNode;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.PostConstruct;

import com.arms.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.requirement.reqdifficulty.model.ReqDifficultyDTO;
import com.arms.requirement.reqdifficulty.service.ReqDifficulty;

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
