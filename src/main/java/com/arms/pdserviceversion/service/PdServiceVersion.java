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
package com.arms.pdserviceversion.service;

import com.arms.pdserviceversion.model.PdServiceVersionDTO;
import com.egovframework.ple.treeframework.springhibernate.service.CoreService;

import java.util.List;

public interface PdServiceVersion extends CoreService {

    public List<PdServiceVersionDTO> getVersionListByPdService(PdServiceVersionDTO pdServiceVersionDTO) throws Exception;

    public List<PdServiceVersionDTO> getVersionListByCids(List<Long> pdServiceVersionDTO) throws Exception;
}