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

import com.arms.pdservice.model.PdServiceDTO;
import com.egovframework.ple.treeframework.springhibernate.service.CoreService;

import java.util.List;

public interface PdService extends CoreService {

    public List<PdServiceDTO> getNodesWithoutRoot(PdServiceDTO pdServiceDTO) throws Exception;

    public PdServiceDTO addNodeToEndPosition(PdServiceDTO pdServiceDTO) throws Exception;

}