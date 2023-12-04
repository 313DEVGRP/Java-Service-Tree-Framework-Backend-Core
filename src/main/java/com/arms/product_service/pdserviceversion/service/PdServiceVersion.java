/*
 * @author Dongmin.lee
 * @since 2022-11-20
 * @version 22.11.20
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.product_service.pdserviceversion.service;

import com.arms.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;
import java.util.Map;

public interface PdServiceVersion extends TreeService {

    public List<PdServiceVersionEntity> getVersionListByPdService(PdServiceVersionEntity pdServiceVersionEntity) throws Exception;

    public List<PdServiceVersionEntity> getVersionListByCids(List<Long> pdServiceVersionDTO) throws Exception;

    Map<Long, String> getVersionStartDates(List<Long> pdServiceVersionList) throws Exception;

    public List<PdServiceVersionEntity> getVersionListByAjax(List<Long> pdServiceVersionList) throws Exception;
}