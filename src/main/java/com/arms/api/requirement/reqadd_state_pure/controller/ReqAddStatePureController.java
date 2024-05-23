package com.arms.api.requirement.reqadd_state_pure.controller;

import com.arms.api.requirement.reqadd_state_pure.model.ReqAddStatePureDTO;
import com.arms.api.requirement.reqadd_state_pure.model.ReqAddStatePureEntity;
import com.arms.api.requirement.reqadd_state_pure.service.ReqAddStatePure;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
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
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/reqAddStatePure"})
public class ReqAddStatePureController extends TreeAbstractController<ReqAddStatePure, ReqAddStatePureDTO, ReqAddStatePureEntity> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("reqAddStatePure")
    private ReqAddStatePure reqAddStatePure;

    @PostConstruct
    public void initialize() {
        setTreeService(reqAddStatePure);
        setTreeEntity(ReqAddStatePureEntity.class);
    }

    @ResponseBody
    @RequestMapping(
            value = {"/{changeReqTableName}/reqProgress.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView reqProgress(@PathVariable(value ="changeReqTableName") String changeReqTableName,
                                    ReqAddStatePureDTO reqAddStatePureDTO, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("ReqAddPureController :: reqProgress");
        Long 제품서비스_아이디 = Long.parseLong(StringUtils.replace(changeReqTableName, "T_ARMS_REQADD_", ""));

        ReqAddStatePureEntity reqAddStatePureEntity = modelMapper.map(reqAddStatePureDTO, ReqAddStatePureEntity.class);
        List<ReqAddStatePureEntity> list = reqAddStatePure.reqProgress(reqAddStatePureEntity, changeReqTableName, 제품서비스_아이디, reqAddStatePureEntity.getC_req_pdservice_versionset_link(), request);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);

        return modelAndView;
    }
}