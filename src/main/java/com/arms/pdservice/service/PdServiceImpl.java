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
package com.arms.pdservice.service;

import com.arms.dynamicdbmaker.service.DynamicDBMaker;
import com.arms.filerepository.model.FileRepositoryEntity;
import com.arms.filerepository.service.FileRepository;
import com.arms.pdservice.model.PdServiceD3Chart;
import com.arms.pdservice.model.PdServiceEntity;
import com.arms.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.pdserviceversion.service.PdServiceVersion;
import com.egovframework.javaservice.treeframework.TreeConstant;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.*;
import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import com.egovframework.javaservice.treemap.service.GlobalTreeMapService;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;

@AllArgsConstructor
@Service("pdService")
public class PdServiceImpl extends TreeServiceImpl implements PdService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("fileRepository")
    private FileRepository fileRepository;

    @Autowired
    @Qualifier("pdServiceVersion")
    private PdServiceVersion pdServiceVersion;

    @Autowired
    @Qualifier("dynamicDBMaker")
    private DynamicDBMaker dynamicDBMaker;

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Override
    public List<PdServiceEntity> getNodesWithoutRoot(PdServiceEntity pdServiceEntity) throws Exception {
        pdServiceEntity.setOrder(Order.desc("c_id"));
        Criterion criterion = Restrictions.not(
                // replace "id" below with property name, depending on what you're filtering against
                Restrictions.in("c_id", new Object[] {TreeConstant.ROOT_CID, TreeConstant.First_Node_CID})
        );
        pdServiceEntity.getCriterions().add(criterion);
        List<PdServiceEntity> list = this.getChildNode(pdServiceEntity);
        for (PdServiceEntity dto: list) {
            dto.setC_pdservice_contents("force empty");
        }
        return list;
    }


    @Override
    public PdServiceEntity addNodeToEndPosition(PdServiceEntity pdServiceEntity) throws Exception {
        //루트 노드를 기준으로 리스트를 검색
        PdServiceEntity paramPdServiceEntity = new PdServiceEntity();
        paramPdServiceEntity.setWhere("c_parentid", TreeConstant.First_Node_CID);
        List<PdServiceEntity> list = this.getChildNode(paramPdServiceEntity);

        //검색된 노드중 maxPosition을 찾는다.
        PdServiceEntity maxPositionPdServiceEntity = list
                .stream()
                .max(Comparator.comparing(PdServiceEntity::getC_position))
                .orElseThrow(NoSuchElementException::new);

        //노드 값 셋팅
        pdServiceEntity.setRef(TreeConstant.First_Node_CID);
        pdServiceEntity.setC_position(maxPositionPdServiceEntity.getC_position() + 1);
        pdServiceEntity.setC_type(TreeConstant.Leaf_Node_TYPE);

        return this.addNode(pdServiceEntity);
    }

    @Override
    @Transactional
    public PdServiceEntity addPdServiceAndVersion(PdServiceEntity pdServiceEntity) throws Exception {
        pdServiceEntity.setC_title(Util_TitleChecker.StringReplace(pdServiceEntity.getC_title()));


        //Default Version 생성
        PdServiceVersionEntity baseVerNode = new PdServiceVersionEntity();
        baseVerNode.setRef(TreeConstant.First_Node_CID);
        baseVerNode.setC_title("BaseVersion");
        baseVerNode.setC_type(TreeConstant.Leaf_Node_TYPE);
        baseVerNode.setC_pds_version_start_date("start");
        baseVerNode.setC_pds_version_end_date("end");
        baseVerNode.setC_pds_version_contents("contents");
        baseVerNode.setC_pds_version_etc("etc");
        PdServiceVersionEntity baseNode = pdServiceVersion.addNode(baseVerNode);

        Set<PdServiceVersionEntity> treeset = new HashSet<>();
        treeset.add(baseNode);

        pdServiceEntity.setPdServiceVersionEntities(treeset);

        //제품(서비스) 데이터 등록
        PdServiceEntity addedNode = this.addNode(pdServiceEntity);

        //제품(서비스) 생성시 - 요구사항 TABLE 생성
        //제품(서비스) 생성시 - 요구사항 STATUS TABLE 생성
        dynamicDBMaker.createSchema(addedNode.getC_id().toString());

        //C_ETC 컬럼에 요구사항 테이블 이름 기입
        addedNode.setC_pdservice_etc(TreeConstant.REQADD_PREFIX_TABLENAME + addedNode.getC_id().toString());
        this.updateNode(addedNode);

        return addedNode;
    }

    @Override
    @Transactional
    public PdServiceEntity addPdServiceVersion(PdServiceEntity pdServiceEntity) throws Exception {

        PdServiceEntity pdServiceNode = this.getNode(pdServiceEntity);
        Set<PdServiceVersionEntity> versionNodes = pdServiceNode.getPdServiceVersionEntities();
        //Set<FileRepositoryEntity> fileNodes = pdServiceNode.getFiles();

        //pdServiceNode.setFiles(fileNodes);
        pdServiceNode.setPdServiceVersionEntities(new HashSet<>());
        this.updateNode(pdServiceNode);

        Set<PdServiceVersionEntity> targetVersionNode = pdServiceEntity.getPdServiceVersionEntities();
        Set<PdServiceVersionEntity> addedNode = new HashSet<>();
        for( PdServiceVersionEntity versionEntity : targetVersionNode ){

            String compareTitle = versionEntity.getC_title();
            Optional<PdServiceVersionEntity> searching = versionNodes.stream().filter(node -> StringUtils.equalsIgnoreCase(compareTitle, node.getC_title())).findFirst();

            if( searching.isEmpty()){
                PdServiceVersionEntity addedVersion = pdServiceVersion.addNode(versionEntity);
                addedNode.add(addedVersion);
            }

        }

        Set<PdServiceVersionEntity> mergedSet = new HashSet<>();
        mergedSet.addAll(versionNodes);
        mergedSet.addAll(addedNode);


        //pdServiceNode.setFiles(fileNodes);
        pdServiceNode.setPdServiceVersionEntities(mergedSet);

        this.updateNode(pdServiceNode);

        return pdServiceNode;
    }

    @Override
    @Transactional
    public PdServiceEntity updatePdServiceVersion(Long pdservice_link, PdServiceVersionEntity pdServiceVersionEntity) throws Exception {

        PdServiceEntity searchNode = new PdServiceEntity();
        searchNode.setC_id(pdservice_link);
        PdServiceEntity savedNode = this.getNode(searchNode);

        Set<PdServiceVersionEntity> versionEntityList = savedNode.getPdServiceVersionEntities();
        for ( PdServiceVersionEntity versionEntity : versionEntityList){
            if(versionEntity.getC_id().equals(pdServiceVersionEntity.getC_id())){
                versionEntity.setC_pds_version_start_date(pdServiceVersionEntity.getC_pds_version_start_date());
                versionEntity.setC_pds_version_end_date(pdServiceVersionEntity.getC_pds_version_end_date());
                versionEntity.setC_pds_version_etc(pdServiceVersionEntity.getC_pds_version_etc());
                versionEntity.setC_pds_version_contents(pdServiceVersionEntity.getC_pds_version_contents());
                pdServiceVersion.updateNode(versionEntity);
            }
        }


        return savedNode;
    }

    @Override
    @Transactional
    public Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdservice_link, MultipartHttpServletRequest multiRequest) throws Exception {

        Set<FileRepositoryEntity> fileEntitySet = upload(multiRequest, fileRepository);

        Set<Long> fileCids = new HashSet<>();

        for ( FileRepositoryEntity file : fileEntitySet ){

            GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
            globalTreeMap.setPdservice_link(pdservice_link);
            globalTreeMap.setFilerepository_link(file.getC_id());
            List<GlobalTreeMapEntity> searchList = globalTreeMapService.findAllBy(globalTreeMap);
            if ( searchList == null || searchList.isEmpty() ){
                GlobalTreeMapEntity savedMap = globalTreeMapService.save(globalTreeMap);
                fileCids.add(savedMap.getFilerepository_link());
            } else {
                logger.info("already registe PdService = " + pdservice_link + " & FileRepo = " + globalTreeMap.getFilerepository_link());
                fileCids.add(globalTreeMap.getFilerepository_link());
            }

        }

        Set<FileRepositoryEntity> returnSet = new HashSet<>();
        for (Long fileCid : fileCids ) {

            FileRepositoryEntity entity = new FileRepositoryEntity();
            entity.setC_id(fileCid);
            returnSet.add(fileRepository.getNode(entity));

        }

        return returnSet;
    }

    public Set<FileRepositoryEntity> upload(MultipartHttpServletRequest multiRequest,
                                                   FileRepository fileRepository) throws Exception {

        // Spring multipartResolver 사용시
        PropertiesReader propertiesReader = new PropertiesReader("com/egovframework/property/globals.properties");
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
            //TODO: 썸네일 개발 필요
            fileRepositoryEntity.setThumbnailUrl(egovFormBasedFileVo.getThumbnailUrl());

            fileRepositoryEntity.setDelete_url(egovFormBasedFileVo.getDelete_url());
            fileRepositoryEntity.setDelete_type(egovFormBasedFileVo.getDelete_type());

            fileRepositoryEntity.setRef(TreeConstant.First_Node_CID);
            fileRepositoryEntity.setC_title("for PdService");
            fileRepositoryEntity.setC_type("default");

            FileRepositoryEntity returnFileRepositoryEntity = fileRepository.addNode(fileRepositoryEntity);
            //delete 파라미터인 id 값을 업데이트 치기 위해서.
            fileRepositoryEntity.setUrl("/auth-user/api/arms/fileRepository" + "/downloadFileByNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setThumbnailUrl("/auth-user/api/arms/fileRepository" + "/thumbnailUrlFileToNode/" + returnFileRepositoryEntity.getId());
            fileRepositoryEntity.setDelete_url("/auth-user/api/arms/fileRepository" + "/deleteFileByNode/" + returnFileRepositoryEntity.getId());

            fileRepository.updateNode(fileRepositoryEntity);

            fileRepositoryEntities.add(fileRepositoryEntity);

        }

        return fileRepositoryEntities;
    }

    @Override
    @Transactional
    public PdServiceEntity removeVersionNode(Long pdServiceID, Long versionID) throws Exception {

        PdServiceEntity pdService = new PdServiceEntity();
        pdService.setC_id(pdServiceID);
        PdServiceEntity savedPdServiceNode = this.getNode(pdService);

        Set<PdServiceVersionEntity> versionSet = savedPdServiceNode.getPdServiceVersionEntities();
        for( PdServiceVersionEntity versionEntity : versionSet ){
            if(versionEntity.getC_id().equals(versionID)){
                versionSet.remove(versionEntity);
            }
        }

        pdService.setPdServiceVersionEntities(versionSet);

        this.updateNode(pdService);
        return pdService;
    }

    @Override
    public PdServiceD3Chart getD3ChartData() throws Exception {
        PdServiceEntity pdServiceEntity = new PdServiceEntity();
        List<PdServiceEntity> pdServiceEntityList = this.getNodesWithoutRoot(pdServiceEntity);

        if (!pdServiceEntityList.isEmpty()) {
            List<PdServiceD3Chart> returnList = new ArrayList<>();
            for (PdServiceEntity entity : pdServiceEntityList) {
                Set<PdServiceVersionEntity> versionEntitySet = entity.getPdServiceVersionEntities();
                List<PdServiceD3Chart> versionEntityList = new ArrayList<>();

                if (!versionEntitySet.isEmpty()) {
                    for (PdServiceVersionEntity versionEntity : versionEntitySet) {
                        versionEntityList.add(
                                PdServiceD3Chart.builder()
                                        .type("Version")
                                        .name(versionEntity.getC_title())
                                        .build()
                        );
                    }
                    returnList.add(
                            PdServiceD3Chart.builder()
                                    .type("PdService")
                                    .name(entity.getC_title())
                                    .children(versionEntityList)
                                    .build()
                    );
                } else {
                    returnList.add(
                            PdServiceD3Chart.builder()
                                    .type("PdService")
                                    .name(entity.getC_title())
                                    .build()
                    );
                }
            }
            return PdServiceD3Chart.builder()
                    .name("a-RMS")
                    .children(returnList)
                    .build();
        }
        return null;
    }
}