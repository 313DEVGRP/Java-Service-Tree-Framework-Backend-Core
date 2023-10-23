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
package com.arms.requirement.reqdifficulty.service;

import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;


@Slf4j
@AllArgsConstructor
@Service("reqDifficulty")
public class ReqDifficultyImpl extends TreeServiceImpl implements ReqDifficulty{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());


}