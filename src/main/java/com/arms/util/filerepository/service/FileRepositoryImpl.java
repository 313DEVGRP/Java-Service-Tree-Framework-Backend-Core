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
package com.arms.util.filerepository.service;

import com.arms.util.filerepository.model.FileRepositoryEntity;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.ParameterParser;
import com.egovframework.javaservice.treemap.model.GlobalTreeMapEntity;
import com.egovframework.javaservice.treemap.service.GlobalTreeMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("fileRepository")
public class FileRepositoryImpl extends TreeServiceImpl implements FileRepository{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GlobalTreeMapService globalTreeMapService;

    @Override
    @Transactional
    public HashMap<String, Set<FileRepositoryEntity>> getFileSetByFileIdLink(ParameterParser parser) throws Exception {
        GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
        globalTreeMap.setPdservice_link(parser.getLong("fileIdLink"));
        List<GlobalTreeMapEntity> treeMapListByFileIdLink = globalTreeMapService.findAllBy(globalTreeMap);

        Set<FileRepositoryEntity> returnFileSet = new HashSet<>();
        HashMap<String, Set<FileRepositoryEntity>> returnMap = new HashMap();

        for( GlobalTreeMapEntity row : treeMapListByFileIdLink ){
            if ( row.getFilerepository_link() != null ){
                logger.info("row.getFilerepository_link() = " + row.getFilerepository_link());
                FileRepositoryEntity entity = new FileRepositoryEntity();
                entity.setC_id(row.getFilerepository_link());
                returnFileSet.add(this.getNode(entity));
            }
        }
        returnMap.put("files", returnFileSet);
        return returnMap;
    }

}