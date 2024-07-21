/*
 * @author Dongmin.lee
 * @since 2023-10-22
 * @version 23.10.22
 * @see <pre>
 *  Copyright (C) 2007 by 313 DEV GRP, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by 313 developer group <313@313.co.kr>, December 2010
 * </pre>
 */
package com.arms.api.requirement.reqstate_category_log.service;

import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service("reqStateCategoryLog")
public class ReqStateCategoryLogImpl extends TreeServiceImpl implements ReqStateCategoryLog {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
}