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
package com.arms.api.product_service.pdserviceversion.controller;

import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/arms/pdServiceVersion"})
public class PdServiceVersionController extends TreeAbstractController<PdServiceVersion, PdServiceVersionDTO, PdServiceVersionEntity> {

    private final PdServiceVersion pdServiceVersion;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServiceVersion);
        setTreeEntity(PdServiceVersionEntity.class);
    }

    /**
     * Dashboard, TopMenu
     */
    @GetMapping("/getVersionListBy.do")
    public ModelAndView getVersionListByAjax(@RequestParam("c_ids") List<Long> c_ids) throws Exception {

        log.info("[PdServiceVersionController :: getVersionStartEndDates] :: c_ids => {}", c_ids);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdServiceVersion.getVersionListByAjax(c_ids));

        return modelAndView;
    }

}
