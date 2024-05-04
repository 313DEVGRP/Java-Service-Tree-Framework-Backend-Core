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
package com.arms.api.product_service.pdservice_detail.controller;

import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailDTO;
import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailEntity;
import com.arms.api.product_service.pdservice_detail.service.PdServiceDetail;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.util.filerepository.service.FileRepository;
import com.arms.egovframework.javaservice.treeframework.controller.CommonResponse;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/arms/pdServiceDetail"})
public class PdServiceDetailController extends TreeAbstractController<PdServiceDetail, PdServiceDetailDTO, PdServiceDetailEntity> {

    private final PdServiceDetail pdServiceDetail;

    private final FileRepository fileRepository;

    @PostConstruct
    public void initialize() {
        setTreeService(pdServiceDetail);
        setTreeEntity(PdServiceDetailEntity.class);
    }

    @GetMapping("getNodes.do/{pdServiceId}")
    public ResponseEntity<CommonResponse.ApiResult<List<PdServiceDetailDTO>>> getNodesByPdService(@PathVariable(value = "pdServiceId") Long pdServiceId) throws Exception {

        List<PdServiceDetailEntity> nodesByPdService = pdServiceDetail.getNodesByPdService(pdServiceId);

        List<PdServiceDetailDTO> pdServiceDetailDTOS = nodesByPdService.stream().map(entity -> modelMapper.map(entity, PdServiceDetailDTO.class)).collect(Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(pdServiceDetailDTOS));
    }


    @PostMapping("addNode.do/{pdServiceId}")
    public ResponseEntity<CommonResponse.ApiResult<PdServiceDetailEntity>> addNodeByPdService(@PathVariable(value = "pdServiceId") Long pdServiceId, PdServiceDetailDTO pdServiceDetailDTO) throws Exception {

        PdServiceDetailEntity pdServiceDetailEntity = modelMapper.map(pdServiceDetailDTO, PdServiceDetailEntity.class);

        PdServiceDetailEntity result = pdServiceDetail.addNodeWithGlobalContentsTreeMap(pdServiceId, pdServiceDetailEntity);

        return ResponseEntity.ok(CommonResponse.success(result));
    }

    @PostMapping("/uploadFileToNode.do")
    public ModelAndView uploadFileToNode(final MultipartHttpServletRequest multiRequest, HttpServletRequest request) throws Exception {
        ParameterParser parser = new ParameterParser(request);
        long pdServiceDetailId = parser.getLong("pdServiceDetailId");

        HashMap<String, Set<FileRepositoryEntity>> map = new HashMap();

        map.put("files", pdServiceDetail.uploadFileForPdServiceNode(pdServiceDetailId, multiRequest));

        ModelAndView modelAndView = new ModelAndView("jsonView");
        modelAndView.addObject("result", map);

        return modelAndView;
    }

    @GetMapping("/getFilesByNode.do")
    public ResponseEntity<CommonResponse.ApiResult<HashMap<String, Set<FileRepositoryEntity>>>> getFilesByNode(HttpServletRequest request) throws Exception {

        ParameterParser parser = new ParameterParser(request);

        HashMap<String, Set<FileRepositoryEntity>> returnMap = fileRepository.getFileSetByFileIdLinkWithGlobalContentsMap(parser);

        return ResponseEntity.ok(CommonResponse.success(returnMap));
    }

}