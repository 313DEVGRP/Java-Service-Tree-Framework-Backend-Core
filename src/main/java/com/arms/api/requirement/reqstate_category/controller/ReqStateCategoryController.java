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
package com.arms.api.requirement.reqstate_category.controller;


import com.arms.api.requirement.reqstate_category.model.ReqStateCategoryDTO;
import com.arms.api.requirement.reqstate_category.model.ReqStateCategoryEntity;
import com.arms.api.requirement.reqstate_category.service.ReqStateCategory;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqStateCategory"})
public class ReqStateCategoryController extends TreeAbstractController<ReqStateCategory, ReqStateCategoryDTO, ReqStateCategoryEntity> {

    @Autowired
    @Qualifier("reqStateCategory")
    private ReqStateCategory reqStateCategory;

    @PostConstruct
    public void initialize() {
        setTreeService(reqStateCategory);
		setTreeEntity(ReqStateCategoryEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/getClosedCategory.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> 완료상태_카테고리_조회() throws Exception {

        logger.info(" [ 완료상태_카테고리_조회 ]");
        ReqStateCategoryEntity reqStateCategoryEntity = new ReqStateCategoryEntity();
        reqStateCategoryEntity.getCriterions().add(Restrictions.eq("c_closed", "true"));
        List<ReqStateCategoryEntity> 완료상태_카테고리목록 = reqStateCategory.getNodesWithoutRoot(reqStateCategoryEntity);

        // 맵
        /*Function<ReqStateCategoryEntity, Long> key = ReqStateCategoryEntity::getC_id;
        Function<ReqStateCategoryEntity, ReqStateCategoryEntity> value = Function.identity();
        Map<Long, ReqStateCategoryEntity> 완료상태_카테고리맵 = reqStateCategory.getNodesWithoutRootMap(reqStateCategoryEntity, key, value);*/

        return ResponseEntity.ok(완료상태_카테고리목록);
    }
}
