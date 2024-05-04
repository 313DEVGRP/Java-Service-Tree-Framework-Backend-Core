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


import com.arms.api.product_service.pdservice_detail.model.PdServiceDetailEntity;
import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Set;

public interface PdServiceDetail extends TreeService {

    List<PdServiceDetailEntity> getNodesByPdService(Long pdServiceId) throws Exception;

    PdServiceDetailEntity addNodeWithGlobalContentsTreeMap(Long pdServiceId, PdServiceDetailEntity pdServiceDetailEntity) throws Exception;

    Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdServiceDetailId,  MultipartHttpServletRequest multiRequest) throws Exception;

}