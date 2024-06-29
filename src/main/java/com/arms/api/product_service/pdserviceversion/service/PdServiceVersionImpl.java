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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("pdServiceVersion")
public class PdServiceVersionImpl extends TreeServiceImpl implements PdServiceVersion {

    @Override
    public List<PdServiceVersionEntity> getVersionListByCids(List<Long> cids) throws Exception {

        PdServiceVersionEntity versionDTO = new PdServiceVersionEntity();
        Criterion criterion = Restrictions.in("c_id", cids);

        versionDTO.getCriterions().add(criterion);

        List<PdServiceVersionEntity> pdServiceVersionEntities = this.getChildNode(versionDTO);
        log.info("UserPdServiceVersionController ::  getVersions :: pdServiceVersionDTOS = " + pdServiceVersionEntities.size());

        return pdServiceVersionEntities;
    }


    @Override
    public List<PdServiceVersionEntity> getVersionListByAjax(List<Long> pdServiceVersionList) throws Exception {
        PdServiceVersionEntity 버전_검색세팅 = new PdServiceVersionEntity();
        버전_검색세팅.setWhereIn("c_id", pdServiceVersionList);
        List<PdServiceVersionEntity> 검색결과 = this.getChildNode(버전_검색세팅);

        log.info("PdServiceVersionImpl ::  getVersionList.size = {}", 검색결과.size());

        return 검색결과;
    }

    @Override
    public Map<String, String> versionPeriod(List<Long> c_ids) throws Exception {
        PdServiceVersionEntity 버전_검색세팅 = new PdServiceVersionEntity();
        버전_검색세팅.setWhereIn("c_id", c_ids);
        List<PdServiceVersionEntity> 검색결과_목록 = this.getChildNode(버전_검색세팅);
        log.info("PdServiceVersionImpl ::  versionPeriod.size = {}", 검색결과_목록.size());

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
        log.info("[PdServiceVersionImpl ::  versionPeriod] :: earliestDate -> {}, latestDate -> {}  = {}",
                earliestDate, latestDate);
        return map;
    }

}