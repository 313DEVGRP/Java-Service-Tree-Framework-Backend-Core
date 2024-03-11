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
package com.arms.api.product_service.pdserviceversion.service;

import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<Long, String> getVersionStartDates(List<Long> pdServiceVersionList) throws Exception {

        Map<Long, String> 버전_시작일 = new HashMap<>();
        for (Long c_id : pdServiceVersionList) {
            PdServiceVersionEntity pdServiceVersionEntity = new PdServiceVersionEntity();
            pdServiceVersionEntity.setC_id(c_id);
            PdServiceVersionEntity 버전정보 = this.getNode(pdServiceVersionEntity);
            String 시작일 = 버전정보.getC_pds_version_start_date();
            버전_시작일.put(c_id, 시작일);
        }

        return 버전_시작일;
    }

    @Override
    public List<PdServiceVersionEntity> getVersionListByAjax(List<Long> pdServiceVersionList) throws Exception {
        PdServiceVersionEntity 버전_검색세팅 = new PdServiceVersionEntity();
        버전_검색세팅.setWhereIn("c_id",pdServiceVersionList);
        List<PdServiceVersionEntity> 검색결과 = this.getChildNode(버전_검색세팅);

        logger.info("PdServiceVersionImpl ::  getVersionList.size = {}", 검색결과.size());

        return 검색결과;
    }

    @Override
    public Map<String,String> versionPeriod(List<Long> c_ids) throws Exception {
        PdServiceVersionEntity 버전_검색세팅 = new PdServiceVersionEntity();
        버전_검색세팅.setWhereIn("c_id",c_ids);
        List<PdServiceVersionEntity> 검색결과_목록 = this.getChildNode(버전_검색세팅);
        logger.info("PdServiceVersionImpl ::  versionPeriod.size = {}", 검색결과_목록.size());

        Map<String, String> map = new HashMap<>();
        String earliestDate = null;
        String latestDate = null;
        for (PdServiceVersionEntity 버전엔티티 : 검색결과_목록) {
            String start = 버전엔티티.getC_pds_version_start_date();
            String end = 버전엔티티.getC_pds_version_end_date();
            if (earliestDate == null || start.compareTo(earliestDate) < 0) {
                earliestDate = start;
            }
            if (latestDate == null || end.compareTo(latestDate) > 0) {
                latestDate = end;
            }
        }
        map.put("earliestDate", earliestDate);
        map.put("latestDate", latestDate);
        logger.info("[PdServiceVersionImpl ::  versionPeriod] :: earliestDate -> {}, latestDate -> {}  = {}",
                earliestDate, latestDate);
        return map;
    }

}