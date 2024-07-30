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


import com.arms.api.requirement.reqstate.model.ReqStateDTO;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstate_category.model.ReqStateCategoryEntity;
import com.arms.api.requirement.reqstate_category.service.ReqStateCategory;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.validation.group.UpdateNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private ArmsDetailUrlConfig armsDetailUrlConfig;

    @Autowired
    @Qualifier("reqStateCategory")
    private ReqStateCategory reqStateCategory;

    @ResponseBody
    @RequestMapping(value = "/updateNode.do", method = RequestMethod.PUT)
    public ModelAndView updateNode(@Validated(value = UpdateNode.class) ReqStateDTO reqStateDTO,
                                   BindingResult bindingResult, HttpServletRequest request, ModelMap model) throws Exception {

        log.info("ReqStateController :: updateNode");
        ReqStateEntity reqStateEntity = modelMapper.map(reqStateDTO, ReqStateEntity.class);

        if (reqStateDTO.getC_state_category_mapping_id() != null) {
            ReqStateCategoryEntity searchEntity = new ReqStateCategoryEntity();
            searchEntity.setC_id(reqStateDTO.getC_state_category_mapping_id());
            ReqStateCategoryEntity reqStateCategoryEntity = reqStateCategory.getNode(searchEntity);
            reqStateEntity.setReqStateCategoryEntity(reqStateCategoryEntity);
        }

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", reqState.updateNode(reqStateEntity));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/complete-keyword"},
            method = {RequestMethod.GET}
    )
    public ModelAndView 요구사항_완료_키워드조회() {
        logger.info(" [ " + this.getClass().getName() + " :: 요구사항_완료_키워드조회 ]");
        Set<String> 완료_키워드_셋 = new HashSet<>();
        if (armsDetailUrlConfig != null && armsDetailUrlConfig.getCompleteKeyword() != null) {
            String[] keywords = armsDetailUrlConfig.getCompleteKeyword().split(",");
            완료_키워드_셋.addAll(Arrays.asList(keywords));
        }

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", 완료_키워드_셋);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getReqStateListFilter.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> 카테고리_매핑된_상태목록_조회() throws Exception {

        logger.info(" [ 카테고리_매핑된_상태목록_조회 ]");
        ReqStateEntity reqStateEntity = new ReqStateEntity();
        List<ReqStateEntity> 전체_상태목록 = reqState.getNodesWithoutRoot(reqStateEntity);
        List<ReqStateEntity> 카테고리_매핑된_상태목록 = 전체_상태목록.stream()
                                                        .filter(Objects::nonNull)
                                                        .filter(reqState -> reqState.getReqStateCategoryEntity() != null)
                                                        .sorted()
                                                        .collect(Collectors.toList());

        return ResponseEntity.ok(카테고리_매핑된_상태목록);
    }
}
