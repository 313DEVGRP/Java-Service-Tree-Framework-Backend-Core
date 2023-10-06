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
import com.egovframework.javaservice.treeframework.dao.TreeDao;
import com.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqComment"})
public class ReqCommentController extends TreeAbstractController<ReqComment, ReqCommentDTO, ReqCommentEntity> {

    @Autowired
    @Qualifier("reqComment")
    private ReqComment reqComment;

    @Resource(name = "treeDao")
    private TreeDao treeDao;

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
            value = {"/getUserReqCommentList.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getUserReqCommentList(ReqCommentDTO reqCommentDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: getReqCommentList");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);
        List<ReqCommentEntity> reqCommentEntities = reqComment.getNodesWithoutRoot(reqCommentEntity);

        List<ReqCommentEntity> result = reqCommentEntities.stream().filter(data ->
                data.getC_req_comment_sender().equals(reqCommentDTO.getC_req_comment_sender())
        ).collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(result));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/getReqCommentChildNode.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getReqCommentChildNode(ReqCommentDTO reqCommentDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: getReqCommentChildNode");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);

        Criterion criterion1 = Restrictions.eq("c_pdservice_link", reqCommentDTO.getC_pdservice_link());
        Criterion criterion2 = Restrictions.eq("c_req_link", reqCommentDTO.getC_req_link());
        Criterion criterion = Restrictions.and(criterion1, criterion2);
        reqCommentEntity.getCriterions().add(criterion);
        reqCommentEntity.getOrder().add(Order.desc("c_req_comment_date")); // 최신 시간으로 정렬
       
        List<ReqCommentEntity> list = reqComment.getChildNode(reqCommentEntity);

        return ResponseEntity.ok(CommonResponse.success(list));

    }

    @Transactional
    @ResponseBody
    @RequestMapping(
                    value = {"/getReqCommentPagingByPdService.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getReqCommentPagingByPdService(ReqCommentDTO reqCommentDTO
            , ModelMap model
            , HttpServletRequest request
            , @RequestParam int pageIndex
            , @RequestParam int pageUnit) throws Exception {

        log.info("ReqCommentController :: getReqCommentPagingByPdService");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);

        Criterion search_pdservice = Restrictions.eq("c_pdservice_link", reqCommentDTO.getC_pdservice_link());
        Criterion search_req = Restrictions.eq("c_req_link", reqCommentDTO.getC_req_link());
        Criterion search_criteria = Restrictions.and(search_pdservice, search_req);
        reqCommentEntity.getCriterions().add(search_criteria);
        reqCommentEntity.getOrder().add(Order.desc("c_req_comment_date")); // 최신 시간으로 정렬

        reqCommentEntity.setPageIndex(pageIndex);
        reqCommentEntity.setPageUnit(pageUnit);

        List<ReqCommentEntity> list = reqComment.getPaginatedChildNode(reqCommentEntity);

        return ResponseEntity.ok(CommonResponse.success(list));

    }

    @ResponseBody
    @RequestMapping(
            value = {"/getTotalCountReqComment.do"},
            method = {RequestMethod.GET}
    )
    public ResponseEntity<?> getTotalCountReqComment(ReqCommentDTO reqCommentDTO
            , ModelMap model
            , HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: getTotalCountReqComment");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);

        Criterion search_pdservice = Restrictions.eq("c_pdservice_link", reqCommentDTO.getC_pdservice_link());
        Criterion search_req = Restrictions.eq("c_req_link", reqCommentDTO.getC_req_link());
        Criterion search_criteria = Restrictions.and(search_pdservice, search_req);
        reqCommentEntity.getCriterions().add(search_criteria);

        treeDao.setClazz(reqCommentEntity.getClass());
        int totalCount = treeDao.getCount(reqCommentEntity);

        return ResponseEntity.ok(CommonResponse.success(totalCount));

    }


    @ResponseBody
    @RequestMapping(
            value = {"/addReqComment.do"},
            method = {RequestMethod.POST}
    )
    public ResponseEntity<?> addReqComment(ReqCommentDTO reqCommentDTO
            , ModelMap model
            , HttpServletRequest request) throws Exception {

        log.info("ReqCommentController :: addReqComment");
        ReqCommentEntity reqCommentEntity = modelMapper.map(reqCommentDTO, ReqCommentEntity.class);
        reqCommentEntity.setC_title(Util_TitleChecker.StringReplace(reqCommentEntity.getC_title()));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        reqCommentEntity.setC_req_comment_date(formattedDateTime);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", reqComment.addNode(reqCommentEntity));
        return ResponseEntity.ok(CommonResponse.success(modelAndView));

    }

}
