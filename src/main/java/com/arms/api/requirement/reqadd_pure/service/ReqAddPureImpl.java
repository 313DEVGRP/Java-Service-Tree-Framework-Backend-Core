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
package com.arms.api.requirement.reqadd_pure.service;

import com.arms.api.globaltreemap.service.GlobalTreeMapService;

import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.service.JiraServer;

import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;

import com.arms.api.requirement.reqadd_pure.model.ReqAddPureEntity;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;

import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Service("reqAddPure")
public class ReqAddPureImpl extends TreeServiceImpl implements ReqAddPure {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private com.arms.api.util.communicate.external.엔진통신기 엔진통신기;

	@Autowired
	private com.arms.api.util.communicate.internal.내부통신기 내부통신기;

	@Autowired
	private GlobalTreeMapService globalTreeMapService;

	@Autowired
	@Qualifier("pdServiceVersion")
	private PdServiceVersion pdServiceVersion;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Autowired
	@Qualifier("jiraServer")
	private JiraServer jiraServer;

	@Autowired
	protected Chat chat;

	@Override
	@Transactional
	public ReqAddPureEntity moveReqNode(ReqAddPureEntity reqAddPureEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("moveNode",changeReqTableName);

		ReqAddPureEntity savedReqAddPureEntity = this.moveNode(reqAddPureEntity, request);

		SessionUtil.removeAttribute("moveNode");

		return savedReqAddPureEntity;
	}

}
