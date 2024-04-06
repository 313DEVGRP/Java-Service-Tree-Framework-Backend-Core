/*
 * @author Dongmin.lee
 * @since 2023-03-21
 * @version 23.03.21
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.analysis.salary.service;

import com.arms.api.analysis.salary.model.SalaryLogJdbcDTO;
import com.arms.egovframework.javaservice.treeframework.service.TreeService;

import java.util.List;
import java.util.Map;

public interface SalaryLog extends TreeService {

    List<SalaryLogJdbcDTO> findAllLogs(String cMethod, String startDate, String endDate);

    Map<String, SalaryLogJdbcDTO> findAllLogsToMaps(String cMethod, String startDate, String endDate);

}