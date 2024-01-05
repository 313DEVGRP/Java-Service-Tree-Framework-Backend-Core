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
package com.arms.api.product_service.pdserviceversionlog.service;

import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Service("pdServiceVersionLog")
public class PdServiceVersionLogImpl extends TreeServiceImpl implements PdServiceVersionLog{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

}