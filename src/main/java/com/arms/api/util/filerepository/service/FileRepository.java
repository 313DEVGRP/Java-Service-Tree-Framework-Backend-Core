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

import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;
import com.arms.egovframework.javaservice.treeframework.util.ParameterParser;

import java.util.HashMap;
import java.util.Set;

public interface FileRepository extends TreeService {

    public HashMap<String, Set<FileRepositoryEntity>> getFileSetByFileIdLink(ParameterParser parser) throws Exception;

}