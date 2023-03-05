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
import com.egovframework.ple.treeframework.springhibernate.service.CoreServiceImpl;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pdServiceVersion")
public class PdServiceVersionImpl extends CoreServiceImpl implements PdServiceVersion{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<PdServiceVersionDTO> getVersionListByPdService(PdServiceVersionDTO pdServiceVersionDTO) throws Exception {

        pdServiceVersionDTO.setOrder(Order.asc("c_left"));
        pdServiceVersionDTO.setWhere("c_pdservice_link", pdServiceVersionDTO.getC_id().toString());
        List<PdServiceVersionDTO> pdServiceVersionDTOS = this.getChildNode(pdServiceVersionDTO);
        logger.info("UserPdServiceVersionController ::  getVersion :: pdServiceVersionDTOS = " + pdServiceVersionDTOS.size());

        return pdServiceVersionDTOS;
    }

    @Override
    public List<PdServiceVersionDTO> getVersionListByCids(List<Long> cids) throws Exception {

        PdServiceVersionDTO versionDTO = new PdServiceVersionDTO();
        Criterion criterion = Restrictions.in("c_id", cids);

        versionDTO.getCriterions().add(criterion);

        List<PdServiceVersionDTO> pdServiceVersionDTOS = this.getChildNode(versionDTO);
        logger.info("UserPdServiceVersionController ::  getVersions :: pdServiceVersionDTOS = " + pdServiceVersionDTOS.size());

        return pdServiceVersionDTOS;
    }
}