/*
 * @author Dongmin.lee
 * @since 2022-11-20
 * @version 22.11.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.product_service.pdserviceversion.controller;

import com.arms.product_service.pdservice.service.PdService;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.product_service.pdserviceversion.service.PdServiceVersion;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
@RequestMapping(value = {"/arms/pdServiceVersion"})
public class PdServiceVersionController extends TreeAbstractController<PdServiceVersion, PdServiceVersionDTO, PdServiceVersionEntity> {

    @Autowired
    @Qualifier("pdServiceVersion")
    private PdServiceVersion pdServiceVersion;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServiceVersion);
        setTreeEntity(PdServiceVersionEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/getVersionListByCids.do",method= RequestMethod.GET)
    public ModelAndView getVersionListByCids(PdServiceVersionDTO pdServiceVersionDTO, ModelMap model,
                                             HttpServletRequest request) throws Exception {

        log.info("PdServiceVersionController :: getVersionListByCids");
        PdServiceVersionEntity pdServiceVersionEntity = modelMapper.map(pdServiceVersionDTO, PdServiceVersionEntity.class);

        ParameterParser parser = new ParameterParser(request);
        String parse_c_ids = parser.get("c_ids");
        String[] convert_c_ids = StringUtils.jsonStringifyConvert(parse_c_ids);
        List<Long> longList = new ArrayList<>();
        for (String c_id : convert_c_ids ) {
            longList.add(StringUtils.toLong(c_id));
        }

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionListByCids(longList));
        return modelAndView;
    }


    @ResponseBody
    @RequestMapping(
            value = {"/getVersionStartDates.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getVersionStartDates(PdServiceVersionDTO pdServiceVersionDTO,
                                            @RequestParam("c_ids") List<Long> c_ids) throws Exception {

        log.info("PdServiceVersionController :: getVersionStartDates");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionStartDates(c_ids));

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getVersionListBy.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getVersionListByAjax(PdServiceVersionDTO pdServiceVersionDTO,
                                                @RequestParam("c_ids") List<Long> c_ids) throws Exception {

        log.info("PdServiceVersionController :: getVersionStartEndDates");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionListByAjax(c_ids));

        return modelAndView;
    }
}
