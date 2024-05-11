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
package com.arms.api.product_service.pdservice_detail.service;

import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalContentsTreeMapService;
import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailEntity;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.util.filerepository.service.FileRepository;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;

import com.arms.egovframework.javaservice.treeframework.util.EgovFileUploadUtil;
import com.arms.egovframework.javaservice.treeframework.util.EgovFormBasedFileVo;
import com.arms.egovframework.javaservice.treeframework.util.PropertiesReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("pdServiceDetail")
public class PdServiceDetailImpl extends TreeServiceImpl implements PdServiceDetail {

    private final FileRepository fileRepository;

    private final GlobalContentsTreeMapService globalContentsTreeMapService;


    @Override
    public List<PdServiceDetailEntity> getNodesByPdService(Long pdServiceId) throws Exception {

        List<GlobalContentsTreeMapEntity> globalContentsTreeMapEntityList = globalContentsTreeMapService.findAllByIds(List.of(pdServiceId), "pdservice_link");

        List<Long> pdServiceDetailIds = globalContentsTreeMapEntityList.stream().map(GlobalContentsTreeMapEntity::getPdservicedetail_link).collect(Collectors.toList());

        PdServiceDetailEntity pdServiceDetailEntity = new PdServiceDetailEntity();

        Criterion criterion = Restrictions.in("c_id", pdServiceDetailIds);

        pdServiceDetailEntity.getCriterions().add(criterion);

        pdServiceDetailEntity.setOrder(Order.asc("c_position"));

        List<PdServiceDetailEntity> pdServiceBoardEntities = this.getChildNodeWithoutPaging(pdServiceDetailEntity);

        return pdServiceBoardEntities;
    }

    @Override
    @Transactional
    public PdServiceDetailEntity addNodeWithGlobalContentsTreeMap(Long pdServiceId, PdServiceDetailEntity pdServiceDetailEntity) throws Exception {

        // 1. PdServiceBoardEntity 등록
        PdServiceDetailEntity result = this.addNode(pdServiceDetailEntity);

        // 2. GlobalContentsTreeMapEntity 등록
        GlobalContentsTreeMapEntity globalContentsTreeMapEntity = new GlobalContentsTreeMapEntity();
        globalContentsTreeMapEntity.setPdservice_link(pdServiceId);
        globalContentsTreeMapEntity.setPdservicedetail_link(result.getC_id());

        globalContentsTreeMapService.saveOne(globalContentsTreeMapEntity);

        return result;
    }

    @Override
    @Transactional
    public Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdServiceDetailId, MultipartHttpServletRequest multiRequest) throws Exception {

        Set<FileRepositoryEntity> fileEntitySet = upload(multiRequest, fileRepository);

        Set<Long> fileSet = new HashSet<>();

        for (FileRepositoryEntity file : fileEntitySet) {
            GlobalContentsTreeMapEntity globalContentsTreeMapEntity = new GlobalContentsTreeMapEntity();
            globalContentsTreeMapEntity.setPdservicedetail_link(pdServiceDetailId);
            globalContentsTreeMapEntity.setFilerepository_link(file.getC_id());
            List<GlobalContentsTreeMapEntity> searchList = globalContentsTreeMapService.findAllBy(globalContentsTreeMapEntity);
            if (searchList == null || searchList.isEmpty()) {
                GlobalContentsTreeMapEntity savedMap = globalContentsTreeMapService.saveOne(globalContentsTreeMapEntity);
                fileSet.add(savedMap.getFilerepository_link());
            } else {
                log.info("이미 존재하는 PdServiceBoard = " + pdServiceDetailId + " & FileRepo = " + globalContentsTreeMapEntity.getFilerepository_link());
                fileSet.add(globalContentsTreeMapEntity.getFilerepository_link());
            }

        }

        Set<FileRepositoryEntity> returnSet = new HashSet<>();
        for (Long fileCid : fileSet) {
            FileRepositoryEntity entity = new FileRepositoryEntity();
            entity.setC_id(fileCid);
            returnSet.add(fileRepository.getNode(entity));
        }

        return returnSet;
    }
    public Set<FileRepositoryEntity> upload(MultipartHttpServletRequest multiRequest, FileRepository fileRepository) throws Exception {

        PropertiesReader propertiesReader = new PropertiesReader("com/arms/egovframework/property/globals.properties");
        String uploadDir = propertiesReader.getProperty("Globals.fileStorePath");
        long maxFileSize = TreeConstant.MAX_UPLOAD_FILESIZE;

        List<EgovFormBasedFileVo> list = EgovFileUploadUtil.uploadFiles(multiRequest, uploadDir, maxFileSize);

        Set<FileRepositoryEntity> fileRepositoryEntities = new HashSet<>();

        for (EgovFormBasedFileVo egovFormBasedFileVo : list) {

            FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
            fileRepositoryEntity.setFileName(egovFormBasedFileVo.getFileName());
            fileRepositoryEntity.setContentType(egovFormBasedFileVo.getContentType());
            fileRepositoryEntity.setServerSubPath(egovFormBasedFileVo.getServerSubPath());
            fileRepositoryEntity.setPhysicalName(egovFormBasedFileVo.getPhysicalName());
            fileRepositoryEntity.setSize(egovFormBasedFileVo.getSize());
            fileRepositoryEntity.setName(egovFormBasedFileVo.getName());

            fileRepositoryEntity.setUrl(egovFormBasedFileVo.getUrl());

            fileRepositoryEntity.setThumbnailUrl(egovFormBasedFileVo.getThumbnailUrl());

            fileRepositoryEntity.setDelete_url(egovFormBasedFileVo.getDelete_url());
            fileRepositoryEntity.setDelete_type(egovFormBasedFileVo.getDelete_type());

            fileRepositoryEntity.setRef(TreeConstant.First_Node_CID);
            fileRepositoryEntity.setC_title("for PdServiceDetail");
            fileRepositoryEntity.setC_type("default");

            FileRepositoryEntity returnFileRepositoryEntity = fileRepository.addNode(fileRepositoryEntity);

            fileRepositoryEntity.setUrl("/auth-user/api/arms/fileRepository" + "/downloadFileByNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setThumbnailUrl("/auth-user/api/arms/fileRepository" + "/thumbnailUrlFileToNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setDelete_url("/auth-user/api/arms/pdServiceDetail" + "/deleteFileNode.do/" + returnFileRepositoryEntity.getId());

            fileRepository.updateNode(fileRepositoryEntity);

            fileRepositoryEntities.add(fileRepositoryEntity);

        }

        return fileRepositoryEntities;
    }

    @Override
    @Transactional
    public int deleteFile(Long fileId) throws Exception {
        // 1. FileRepositoryEntity 삭제
        FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
        fileRepositoryEntity.setC_id(fileId);
        fileRepository.removeNode(fileRepositoryEntity); // removeNode 로직을 보면 트리 개념으로 right, left로 삭제를 하니까 다 삭제하는듯

        // 2. GlobalContentsTreeMapEntity 삭제
        globalContentsTreeMapService.deleteByColumnValue("filerepository_link", fileId);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class}, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public int deleteAll(Long pdServiceDetailId) throws Exception {
        // 1. PdServiceBoardEntity 삭제
        PdServiceDetailEntity pdServiceDetailEntity = new PdServiceDetailEntity();
        pdServiceDetailEntity.setC_id(pdServiceDetailId);
        this.removeNode(pdServiceDetailEntity);

        // 2. FileRepository 조회 및 삭제
        List<Long> fileLinks = globalContentsTreeMapService.findAllByIds(List.of(pdServiceDetailId), "pdservicedetail_link").stream()
                .filter(globalContentsTreeMapEntity -> globalContentsTreeMapEntity.getFilerepository_link() != null)
                .map(GlobalContentsTreeMapEntity::getFilerepository_link)
                .collect(Collectors.toList());

        for (Long fileLink : fileLinks) {
            FileRepositoryEntity fileRepositoryEntity = new FileRepositoryEntity();
            fileRepositoryEntity.setC_id(fileLink);
            fileRepository.removeNode(fileRepositoryEntity);
        }

        // 3. GlobalContentsTreeMapEntity 삭제
        globalContentsTreeMapService.deleteByColumnValue("pdservicedetail_link", pdServiceDetailId);

        return 1;
    }
}
