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
package com.arms.api.product_service.pdservice.service;

import com.arms.api.util.filerepository.model.FileRepositoryEntity;
import com.arms.api.product_service.pdservice.model.PdServiceD3Chart;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Set;

public interface PdService extends TreeService {

    List<PdServiceEntity> getNodesWithoutRoot(PdServiceEntity pdServiceEntity) throws Exception;

    PdServiceEntity getNodeWithVersionOrderByCidDesc(PdServiceEntity pdServiceEntity) throws Exception;

    PdServiceEntity addPdServiceAndVersion(PdServiceEntity pdServiceEntity) throws Exception;

    PdServiceEntity addPdServiceVersion(PdServiceEntity pdServiceEntity) throws Exception;

    PdServiceEntity updatePdServiceVersion(Long pdservice_link, PdServiceVersionEntity pdServiceVersionEntity) throws Exception;

    Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdservice_link, MultipartHttpServletRequest multiRequest) throws Exception;

    PdServiceEntity removeVersionNode(Long pdServiceID, Long versionID) throws Exception;

    PdServiceD3Chart getD3ChartData() throws Exception;

    int removeAll(Long pdServiceId) throws Exception;
}