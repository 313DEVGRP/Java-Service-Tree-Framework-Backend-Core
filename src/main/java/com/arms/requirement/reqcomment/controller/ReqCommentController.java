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
package com.arms.requirement.reqcomment.controller;

import com.arms.requirement.reqcomment.model.ReqCommentDTO;
import com.arms.requirement.reqcomment.model.ReqCommentEntity;
import com.arms.requirement.reqcomment.service.ReqComment;
import com.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqComment"})
public class ReqCommentController extends TreeAbstractController<ReqComment, ReqCommentDTO, ReqCommentEntity> {

    @Autowired
    @Qualifier("reqComment")
    private ReqComment reqComment;

    @PostConstruct
    public void initialize() {
        setTreeService(reqComment);
        setTreeEntity(ReqCommentEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(
            value = {"/getReqCommentList.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getReqCommentList(ReqCommentDTO reqCommentDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: getReqCommentList");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);
        List<ReqCommentEntity> reqCommentEntities = reqComment.getNodesWithoutRoot(reqCommentEntity);

        List<ReqCommentEntity> result = reqCommentEntities.stream().filter(data ->
                            data.getC_pdservice_link().equals(reqCommentDTO.getC_pdservice_link()) &&
                            data.getC_req_link().equals(reqCommentDTO.getC_req_link())
        ).collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(result));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/getReqCommentChildNodeWithoutPaging.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getReqCommentChildNodeWithoutPaging(ReqCommentDTO reqCommentDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: getReqCommentChildWithoutPaging");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);
        // List<ReqCommentEntity> reqCommentEntities = reqComment.getChildNodeWithoutPaging(reqCommentEntity);

        /* where 조건 처리 */
        //        reqCommentEntity.setWhere("c_pdservice_link", reqCommentDTO.getC_pdservice_link());

        /* 조건 두개 만든 후 and */
        Criterion criterion1 = Restrictions.eq("c_pdservice_link", reqCommentDTO.getC_pdservice_link());
        Criterion criterion2 = Restrictions.eq("c_req_link", reqCommentDTO.getC_req_link());
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        reqCommentEntity.getCriterions().add(criterion);
        List<ReqCommentEntity> list = reqComment.getChildNode(reqCommentEntity);

/*        List<ReqCommentEntity> result = reqCommentEntities.stream().filter(data ->
                data.getC_pdservice_link().equals(reqCommentDTO.getC_pdservice_link()) &&
                        data.getC_req_link().equals(reqCommentDTO.getC_req_link())
        ).collect(Collectors.toList());*/

        return ResponseEntity.ok(CommonResponse.success(list));

    }
}
