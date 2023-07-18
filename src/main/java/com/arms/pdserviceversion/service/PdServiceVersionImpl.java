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

import com.arms.pdservice.model.PdServiceEntity;
import com.arms.pdserviceversion.model.PdServiceVersionEntity;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.egovframework.javaservice.treeframework.util.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service("pdServiceVersion")
public class PdServiceVersionImpl extends TreeServiceImpl implements PdServiceVersion{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<PdServiceVersionEntity> getVersionListByPdService(PdServiceVersionEntity pdServiceVersionEntity) throws Exception {

        List<PdServiceVersionEntity> pdServiceVersionEntities = this.getChildNode(pdServiceVersionEntity);
        logger.info("UserPdServiceVersionController ::  getVersion :: pdServiceVersionDTOS = " + pdServiceVersionEntities.size());

        return pdServiceVersionEntities;
    }

    @Override
    public List<PdServiceVersionEntity> getVersionListByCids(List<Long> cids) throws Exception {

        PdServiceVersionEntity versionDTO = new PdServiceVersionEntity();
        Criterion criterion = Restrictions.in("c_id", cids);

        versionDTO.getCriterions().add(criterion);

        List<PdServiceVersionEntity> pdServiceVersionEntities = this.getChildNode(versionDTO);
        logger.info("UserPdServiceVersionController ::  getVersions :: pdServiceVersionDTOS = " + pdServiceVersionEntities.size());

        return pdServiceVersionEntities;
    }

}