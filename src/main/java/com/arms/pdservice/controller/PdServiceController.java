/*
 * @author Dongmin.lee
 * @since 2022-06-17
 * @version 22.06.17
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.pdservice.controller;

import com.arms.filerepository.service.FileRepository;
import com.arms.pdservice.model.PdServiceDTO;
import com.arms.pdservice.service.PdService;
import com.arms.pdserviceversion.model.PdServiceVersionDTO;
import com.arms.pdserviceversion.service.PdServiceVersion;
import com.egovframework.ple.treeframework.springhibernate.util.FileHandler;
import com.egovframework.ple.treeframework.springhibernate.controller.CoreAbstractController;
import com.egovframework.ple.treeframework.springhibernate.util.Util_TitleChecker;
import com.egovframework.ple.treeframework.springhibernate.validation.group.AddNode;
import com.egovframework.ple.treeframework.springhibernate.util.ParameterParser;
import com.egovframework.ple.treeframework.springhibernate.util.EgovFormBasedFileVo;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/pdService"})
public class PdServiceController extends CoreAbstractController<PdService, PdServiceDTO> {

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @Autowired
    @Qualifier("fileRepository")
    private FileRepository fileRepository;

    @Autowired
    @Qualifier("pdServiceVersion")
    private PdServiceVersion pdServiceVersion;

    @PostConstruct
    public void initialize() {
        setCoreService(pdService);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String REQ_PREFIX_TABLENAME_BY_PDSERVICE = new String("T_ARMS_REQADD_");

    @ResponseBody
    @RequestMapping(
            value = {"/addPdServiceNode.do"},
            method = {RequestMethod.POST}
    )
    public ModelAndView addPdServiceNode(@Validated({AddNode.class}) PdServiceDTO pdServiceDTO,
                                         BindingResult bindingResult, ModelMap model) throws Exception {

        if (bindingResult.hasErrors()) {
            throw new RuntimeException();
        } else {
            pdServiceDTO.setC_title(Util_TitleChecker.StringReplace(pdServiceDTO.getC_title()));

            //제품(서비스) 데이터 등록
            PdServiceDTO addedNode = pdService.addNode(pdServiceDTO);

            //제품(서비스) 생성시 - 요구사항 TABLE 생성
            //pdService.setDynamicReqAddDB(addedNode);

            //C_ETC 컬럼에 요구사항 테이블 이름 기입
            addedNode.setC_etc(REQ_PREFIX_TABLENAME_BY_PDSERVICE + addedNode.getC_id().toString());
            pdService.updateNode(addedNode);

            //제품(서비스) 생성시 - 요구사항 STATUS TABLE 생성
            //pdService.setDynamicReqStatusDB(addedNode);

            //Default Version 생성
            PdServiceVersionDTO pdServiceVersionDTO = new PdServiceVersionDTO();
            pdServiceVersionDTO.setRef(2L);
            pdServiceVersionDTO.setC_title("BaseVersion");
            pdServiceVersionDTO.setC_type("default");
            pdServiceVersionDTO.setC_pdservice_link(addedNode.getC_id().toString());
            pdServiceVersion.addNode(pdServiceVersionDTO);

            ModelAndView modelAndView = new ModelAndView("jsonView");
            modelAndView.addObject("result", addedNode);
            return modelAndView;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/addEndNodeByRoot.do", method = RequestMethod.POST)
    public ModelAndView addEndNodeByRoot(PdServiceDTO pdServiceDTO,
                                         BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new RuntimeException();

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdService.addNodeToEndPosition(pdServiceDTO));

        return modelAndView;
    }

    /**
     * 이미지 Upload를 처리한다.
     *
     * @param multiRequest
     * @param model
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/uploadFileToNode.do", method = RequestMethod.POST)
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest,
                                         HttpServletRequest request, Model model) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        long fileIdLink = parser.getLong("fileIdLink");
        String c_title = "pdService";

        HashMap<String, List<EgovFormBasedFileVo>> map = FileHandler.upload(multiRequest, fileIdLink, c_title, fileRepository, logger);
        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);

        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(
            value = {"/getPdServiceMonitor.do"},
            method = {RequestMethod.GET}
    )
    public ModelAndView getPdServiceMonitor(PdServiceDTO pdServiceDTO, ModelMap model, HttpServletRequest request) throws Exception {

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", pdService.getNodesWithoutRoot(pdServiceDTO));
        return modelAndView;

    }

}