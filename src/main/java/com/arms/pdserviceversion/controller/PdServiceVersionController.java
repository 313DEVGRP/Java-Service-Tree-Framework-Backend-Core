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
package com.arms.pdserviceversion.controller;

import com.arms.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.pdserviceversion.service.PdServiceVersion;
import com.egovframework.ple.treeframework.springhibernate.util.StringUtility;
import com.egovframework.ple.treeframework.springhibernate.controller.CoreAbstractController;
import com.egovframework.ple.treeframework.springhibernate.util.ParameterParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
@RequestMapping(value = {"/arms/pdServiceVersion"})
public class PdServiceVersionController extends CoreAbstractController<PdServiceVersion, PdServiceVersionDTO> {

    @Autowired
    @Qualifier("pdServiceVersion")
    private PdServiceVersion pdServiceVersion;

    @PostConstruct
    public void initialize() {
        setCoreService(pdServiceVersion);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value="/getVersionList.do",method= RequestMethod.GET)
    public ModelAndView getVersionList(PdServiceVersionDTO pdServiceVersionDTO, ModelMap model,
                                       HttpServletRequest request) throws Exception {

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionListByPdService(pdServiceVersionDTO));
        return modelAndView;
    }

    @RequestMapping(value="/getVersionListByCids.do",method= RequestMethod.GET)
    public ModelAndView getVersionListByCids(PdServiceVersionDTO pdServiceVersionDTO, ModelMap model,
                                             HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        String parse_c_ids = parser.get("c_ids");
        String[] convert_c_ids = StringUtility.jsonStringifyConvert(parse_c_ids);
        List<Long> longList = new ArrayList<>();
        for (String c_id : convert_c_ids ) {
            longList.add(StringUtility.toLong(c_id));
        }

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionListByCids(longList));
        return modelAndView;
    }

    @RequestMapping(value="/updateVersionNode.do", method= RequestMethod.POST)
    public ModelAndView updateVersionNode(PdServiceVersionDTO pdServiceVersionDTO,
                                          BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new RuntimeException();


        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.updateNode(pdServiceVersionDTO));

        return modelAndView;
    }

}
