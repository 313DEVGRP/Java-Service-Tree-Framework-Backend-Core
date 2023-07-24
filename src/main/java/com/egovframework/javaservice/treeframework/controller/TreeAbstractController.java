/*
 * @author Dongmin.lee
 * @since 2023-03-13
 * @version 23.03.13
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.egovframework.javaservice.treeframework.controller;

import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.model.TreeBaseDTO;
import com.egovframework.javaservice.treeframework.model.TreeSearchEntity;
import com.egovframework.javaservice.treeframework.remote.Chat;
import com.egovframework.javaservice.treeframework.service.TreeService;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.Util_TitleChecker;
import com.egovframework.javaservice.treeframework.validation.group.*;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Api("TreeFramework")
public abstract class TreeAbstractController<T extends TreeService, D extends TreeBaseDTO, V extends TreeSearchEntity> {

    private T treeService;
    private Class<V> treeEntity;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected Chat chat;

    public void setTreeService(T treeService) {
        this.treeService = treeService;
    }
    public void setTreeEntity(Class<V> treeEntity) {
        this.treeEntity = treeEntity;
    }

    @ApiOperation(  value = "[Select] TreeFramework의 GetNode" )
    @ResponseBody
    @RequestMapping(value = "/getNode.do", method = RequestMethod.GET)
    public ModelAndView getNode(D treeBaseDTO, HttpServletRequest request) throws Exception {

        log.info("TreeAbstractController :: getNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException("c_id is minus value");
        }

        V returnVO = treeService.getNode(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", returnVO);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getChildNode.do", method = RequestMethod.GET)
    public ModelAndView getChildNode(D treeBaseDTO, HttpServletRequest request)
            throws Exception {

        log.info("TreeAbstractController :: getChildNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        ParameterParser parser = new ParameterParser(request);

        if (parser.getInt("c_id") <= 0) {
            throw new RuntimeException("c_id is minus value");
        }

        treeSearchEntity.setWhere("c_parentid", new Long(parser.get("c_id")));
        List<TreeSearchEntity> list = treeService.getChildNode(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getNodesWithoutRoot.do", method = RequestMethod.GET)
    public ModelAndView getNodesWithoutRoot(D treeBaseDTO) throws Exception {

        log.info("TreeAbstractController :: getNodesWithoutRoot");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeSearchEntity.setOrder(Order.desc("c_id"));
        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in("c_id", new Object[] {TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        treeSearchEntity.getCriterions().add(criterion);
        List<V> list = treeService.getChildNode(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("paginationInfo", treeSearchEntity.getPaginationInfo());
        resultMap.put("result", list);
        modelAndView.addObject("result", resultMap);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getPaginatedChildNode.do", method = RequestMethod.GET)
    public ModelAndView getPaginatedChildNode(D treeBaseDTO, ModelMap model,
                                              HttpServletRequest request) throws Exception {

        log.info("TreeAbstractController :: getPaginatedChildNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        if (treeSearchEntity.getC_id() <= 0 || treeSearchEntity.getPageIndex() <= 0
                || treeSearchEntity.getPageUnit() <= 0 || treeSearchEntity.getPageSize() <= 0) {
            throw new RuntimeException();
        }
        treeSearchEntity.setWhere("c_parentid", treeSearchEntity.getC_id());
        List<TreeSearchEntity> resultChildNodes = treeService.getPaginatedChildNode(treeSearchEntity);
        treeSearchEntity.getPaginationInfo().setTotalRecordCount(resultChildNodes.size());

        ModelAndView modelAndView = new ModelAndView("jsonView");
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("paginationInfo", treeSearchEntity.getPaginationInfo());
        resultMap.put("result", resultChildNodes);
        modelAndView.addObject("result", resultMap);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/searchNode.do", method = RequestMethod.GET)
    public ModelAndView searchNode(D treeBaseDTO, ModelMap model, HttpServletRequest request)
            throws Exception {

        log.info("TreeAbstractController :: searchNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        ParameterParser parser = new ParameterParser(request);

        if (!StringUtils.hasText(request.getParameter("searchString"))) {
            throw new RuntimeException("searchString is null");
        }

        treeSearchEntity.setWhereLike("c_title", parser.get("parser"));
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeService.searchNode(treeSearchEntity));
        return modelAndView;
    }

    @ApiOperation(  value = "[Insert] TreeFramework의 AddNode")
    @ResponseBody
    @RequestMapping(value = "/addNode.do", method = RequestMethod.POST)
    public ModelAndView addNode(@Validated(value = AddNode.class) D treeBaseDTO,
                                BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("TreeAbstractController :: addNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeSearchEntity.setC_title(Util_TitleChecker.StringReplace(treeSearchEntity.getC_title()));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeService.addNode(treeSearchEntity));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/removeNode.do", method = RequestMethod.DELETE)
    public ModelAndView removeNode(@Validated(value = RemoveNode.class) D treeBaseDTO,
                                   BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("TreeAbstractController :: removeNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeSearchEntity.setStatus(treeService.removeNode(treeSearchEntity));
        setJsonDefaultSetting(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeSearchEntity);
        return modelAndView;
    }

    public void setJsonDefaultSetting(V treeSearchEntity) {
        long defaultSettingValue = 0;
        treeSearchEntity.setC_parentid(defaultSettingValue);
        treeSearchEntity.setC_position(defaultSettingValue);
        treeSearchEntity.setC_left(defaultSettingValue);
        treeSearchEntity.setC_right(defaultSettingValue);
        treeSearchEntity.setC_level(defaultSettingValue);
        treeSearchEntity.setRef(defaultSettingValue);
    }

    @ResponseBody
    @RequestMapping(value = "/updateNode.do", method = RequestMethod.PUT)
    public ModelAndView updateNode(@Validated(value = UpdateNode.class) D treeBaseDTO,
                                   BindingResult bindingResult, HttpServletRequest request, ModelMap model) throws Exception {

        log.info("TreeAbstractController :: updateNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeService.updateNode(treeSearchEntity));
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/alterNode.do", method = RequestMethod.PUT)
    public ModelAndView alterNode(@Validated(value = AlterNode.class) D treeBaseDTO,
                                  BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("TreeAbstractController :: alterNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);


        treeSearchEntity.setC_title(Util_TitleChecker.StringReplace(treeSearchEntity.getC_title()));

        treeSearchEntity.setStatus(treeService.alterNode(treeSearchEntity));
        setJsonDefaultSetting(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeSearchEntity);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/alterNodeType.do", method = RequestMethod.PUT)
    public ModelAndView alterNodeType(@Validated(value = AlterNodeType.class) D treeBaseDTO,
                                      BindingResult bindingResult, ModelMap model) throws Exception {

        log.info("TreeAbstractController :: alterNodeType");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeService.alterNodeType(treeSearchEntity);
        setJsonDefaultSetting(treeSearchEntity);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeSearchEntity);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/moveNode.do", method = RequestMethod.POST)
    public ModelAndView moveNode(@Validated(value = MoveNode.class) D treeBaseDTO,
                                 BindingResult bindingResult, ModelMap model, HttpServletRequest request) throws Exception {

        log.info("TreeAbstractController :: moveNode");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeService.moveNode(treeSearchEntity, request);
        setJsonDefaultSetting(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", treeSearchEntity);
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/analyzeNode.do", method = RequestMethod.GET)
    public ModelAndView analyzeNode(ModelMap model) {

        log.info("TreeAbstractController :: analyzeNode");

        model.addAttribute("analyzeResult", "");

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", "true");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "/getMonitor.do", method = RequestMethod.GET)
    public ModelAndView getMonitor(D treeBaseDTO, ModelMap model, HttpServletRequest request)
            throws Exception {

        log.info("TreeAbstractController :: getMonitor");
        V treeSearchEntity = modelMapper.map(treeBaseDTO, treeEntity);

        treeSearchEntity.setOrder(Order.desc("c_id"));
        List<TreeSearchEntity> list = treeService.getChildNode(treeSearchEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", list);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping(value = "/send-message")
    public ResponseEntity<?> sendMessage(@RequestParam("message") String message)  {
        chat.sendMessageByEngine(message);
        return ResponseEntity.ok(CommonResponse.success("OK"));
    }

}