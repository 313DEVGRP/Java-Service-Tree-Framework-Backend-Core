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

import com.arms.filerepository.model.FileRepositoryEntity;
import com.arms.pdservice.model.PdServiceD3Chart;
import com.arms.pdservice.model.PdServiceEntity;
import com.arms.pdserviceversion.model.PdServiceVersionEntity;
import com.egovframework.javaservice.treeframework.service.TreeService;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Set;

public interface PdService extends TreeService {

    public List<PdServiceEntity> getNodesWithoutRoot(PdServiceEntity pdServiceEntity) throws Exception;

    public PdServiceEntity addNodeToEndPosition(PdServiceEntity pdServiceEntity) throws Exception;

    public PdServiceEntity addPdServiceAndVersion(PdServiceEntity pdServiceEntity) throws Exception;

    public PdServiceEntity addPdServiceVersion(PdServiceEntity pdServiceEntity) throws Exception;

    public Set<FileRepositoryEntity> uploadFileForPdServiceNode(Long pdservice_link, MultipartHttpServletRequest multiRequest) throws Exception;

    public PdServiceEntity removeVersionNode(Long pdServiceID, Long versionID) throws Exception;

    public PdServiceD3Chart getD3ChartData() throws Exception;

}