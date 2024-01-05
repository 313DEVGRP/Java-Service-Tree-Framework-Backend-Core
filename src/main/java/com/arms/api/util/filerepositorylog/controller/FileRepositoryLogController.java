/*
 * @author Dongmin.lee
 * @since 2023-03-20
 * @version 23.03.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.util.filerepositorylog.controller;

import com.arms.api.util.filerepositorylog.model.FileRepositoryLogDTO;
import com.arms.egovframework.javaservice.treeframework.controller.TreeAbstractController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import com.arms.api.util.filerepositorylog.model.FileRepositoryLogEntity;
import com.arms.api.util.filerepositorylog.service.FileRepositoryLog;

@Slf4j
@Controller
@RequestMapping(value = {"/arms/fileRepositoryLog"})
public class FileRepositoryLogController extends TreeAbstractController<FileRepositoryLog, FileRepositoryLogDTO, FileRepositoryLogEntity> {

    @Autowired
    @Qualifier("fileRepositoryLog")
    private FileRepositoryLog fileRepositoryLog;

    @PostConstruct
    public void initialize() {
        setTreeService(fileRepositoryLog);
        setTreeEntity(FileRepositoryLogEntity.class);
    }

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}
