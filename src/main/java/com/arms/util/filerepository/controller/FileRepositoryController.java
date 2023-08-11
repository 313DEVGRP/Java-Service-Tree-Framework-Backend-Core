/*
 * @author Dongmin.lee
 * @since 2022-11-04
 * @version 22.11.04
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.util.filerepository.controller;

import com.arms.util.filerepository.model.FileRepositoryDTO;
import com.arms.util.filerepository.model.FileRepositoryEntity;
import com.arms.util.filerepository.service.FileRepository;
import com.arms.product_service.pdservice.service.PdService;
import com.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.egovframework.javaservice.treeframework.util.PropertiesReader;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treeframework.util.EgovFormBasedFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/fileRepository"})
public class FileRepositoryController extends TreeAbstractController<FileRepository, FileRepositoryDTO, FileRepositoryEntity> {

    @Autowired
    @Qualifier("fileRepository")
    private FileRepository fileRepository;

    @Autowired
    @Qualifier("pdService")
    private PdService pdService;

    @PostConstruct
    public void initialize() {
        setTreeService(fileRepository);
        setTreeEntity(FileRepositoryEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/getFilesByNode.do", method = RequestMethod.GET)
    public ModelAndView getFilesByNode(FileRepositoryDTO fileRepositoryDTO, HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);
        HashMap<String, Set<FileRepositoryEntity>> returnMap = fileRepository.getFileSetByFileIdLink(parser);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", returnMap);

        return modelAndView;
    }



    @ResponseBody
    @RequestMapping(value="/deleteFileByNode/{fileId}", method = RequestMethod.DELETE)
    public ModelAndView deleteFileByNode(@PathVariable(value ="fileId") Long fileId, ModelMap model,
                                         HttpServletRequest request) throws Exception {

        FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
        fileRepositoryEntity.setC_id(fileId);
        int result = fileRepository.removeNode(fileRepositoryEntity);

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", result);
        return modelAndView;
    }



    @RequestMapping(value="/downloadFileByNode/{fileId}", method = RequestMethod.GET)
    public void downloadFile(@PathVariable(value ="fileId") Long fileId,
                             HttpServletRequest request, HttpServletResponse response) throws Exception {

        PropertiesReader propertiesReader = new PropertiesReader("com/egovframework/property/globals.properties");
        String uploadPath = propertiesReader.getProperty("Globals.fileStorePath");

        FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
        fileRepositoryEntity.setWhere("c_id", fileId);
        FileRepositoryEntity returnFileRepositoryEntity = fileRepository.getNode(fileRepositoryEntity);

        EgovFormBasedFileUtil.downloadFile(response, uploadPath
                , returnFileRepositoryEntity.getServerSubPath()
                , returnFileRepositoryEntity.getPhysicalName()
                , returnFileRepositoryEntity.getContentType()
                , returnFileRepositoryEntity.getFileName());

    }

}
