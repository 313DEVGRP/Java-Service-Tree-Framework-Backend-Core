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
package com.arms.api.util.filerepository.service;

import com.arms.api.globaltreemap.model.GlobalContentsTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalContentsTreeMapService;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;
import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service("fileRepository")
public class FileRepositoryImpl extends TreeServiceImpl implements FileRepository{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Autowired
    private GlobalContentsTreeMapService globalContentsTreeMapService;

    @Override
    @Transactional
    public HashMap<String, Set<FileRepositoryEntity>> getFileSetByFileIdLink(ParameterParser parser) throws Exception {
        GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
        globalTreeMap.setPdservice_link(parser.getLong("fileIdLink"));
        List<GlobalTreeMapEntity> treeMapListByFileIdLink = globalTreeMapService.findAllBy(globalTreeMap);

        Set<FileRepositoryEntity> returnFileSet = new TreeSet<>(Comparator.comparing(FileRepositoryEntity::getC_id).reversed());
        HashMap<String, Set<FileRepositoryEntity>> returnMap = new HashMap();

        for( GlobalTreeMapEntity row : treeMapListByFileIdLink ){
            if ( row.getFilerepository_link() != null ){
                logger.info("row.getFilerepository_link() = " + row.getFilerepository_link());
                FileRepositoryEntity entity = new FileRepositoryEntity();
                entity.setC_id(row.getFilerepository_link());
                FileRepositoryEntity returnEntity = this.getNode(entity);

                if (returnEntity != null) {
                    returnFileSet.add(returnEntity);
                }

            }
        }
        returnMap.put("files", returnFileSet);
        return returnMap;
    }

    @Override
    public HashMap<String, Set<FileRepositoryEntity>> getFileSetByFileIdLinkWithGlobalContentsMap(ParameterParser parser) throws Exception {
        GlobalContentsTreeMapEntity globalContentsTreeMapEntity = new GlobalContentsTreeMapEntity();
        globalContentsTreeMapEntity.setPdservicedetail_link(parser.getLong("fileIdLink"));
        List<GlobalContentsTreeMapEntity> treeMapListByFileIdLink = globalContentsTreeMapService.findAllBy(globalContentsTreeMapEntity);

        Set<FileRepositoryEntity> returnFileSet = new TreeSet<>(Comparator.comparing(FileRepositoryEntity::getC_id).reversed());
        HashMap<String, Set<FileRepositoryEntity>> returnMap = new HashMap();

        for( GlobalContentsTreeMapEntity row : treeMapListByFileIdLink ){
            if ( row.getFilerepository_link() != null ){
                logger.info("row.getFilerepository_link() = " + row.getFilerepository_link());
                FileRepositoryEntity entity = new FileRepositoryEntity();
                entity.setC_id(row.getFilerepository_link());
                FileRepositoryEntity returnEntity = this.getNode(entity);

                if (returnEntity != null) {
                    returnFileSet.add(returnEntity);
                }

            }
        }
        returnMap.put("files", returnFileSet);
        return returnMap;
    }
}