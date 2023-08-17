/*
 * @author Dongmin.lee
 * @since 2023-03-08
 * @version 23.03.08
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.util.dynamicdbmaker.service;

import com.arms.util.dynamicdbmaker.mapper.DynamicDBMakerDao;
import com.arms.util.dynamicdbmaker.model.DynamicDBMakerEntity;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("dynamicDBMaker")
public class DynamicDBMakerImpl extends TreeServiceImpl implements DynamicDBMaker {

	private static final String REQ_PREFIX_TABLENAME_BY_PDSERVICE = new String("T_ARMS_REQADD_");
	private static final String REQ_PREFIX_TABLENAME_BY_PDSERVICE_STATUS = new String("T_ARMS_REQSTATUS_");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DynamicDBMakerDao dynamicDBMakerDao;

	@Override
	public Integer createSchema(String tableNumber){

		DynamicDBMakerEntity dynamicDBMakerEntity = new DynamicDBMakerEntity();
		dynamicDBMakerEntity.setC_title(REQ_PREFIX_TABLENAME_BY_PDSERVICE + tableNumber);

		Integer makeResult = 1;

		dynamicDBMakerDao.ddlLogExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.ddlOrgExecute(dynamicDBMakerEntity);

		dynamicDBMakerDao.dmlOrgExecute1(dynamicDBMakerEntity);
		dynamicDBMakerDao.dmlOrgExecute2(dynamicDBMakerEntity);

		dynamicDBMakerDao.triggerInsertExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.triggerUpdateExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.triggerDeleteExecute(dynamicDBMakerEntity);

		dynamicDBMakerEntity.setC_title(REQ_PREFIX_TABLENAME_BY_PDSERVICE_STATUS + tableNumber);

		makeResult = 2;

		dynamicDBMakerDao.ddl_statusLogExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.ddl_statusOrgExecute(dynamicDBMakerEntity);

		dynamicDBMakerDao.dml_statusOrgExecute1(dynamicDBMakerEntity);
		dynamicDBMakerDao.dml_statusOrgExecute2(dynamicDBMakerEntity);


		dynamicDBMakerDao.trigger_statusInsertExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.trigger_statusUpdateExecute(dynamicDBMakerEntity);
		dynamicDBMakerDao.trigger_statusDeleteExecute(dynamicDBMakerEntity);

		return makeResult;
	}

}