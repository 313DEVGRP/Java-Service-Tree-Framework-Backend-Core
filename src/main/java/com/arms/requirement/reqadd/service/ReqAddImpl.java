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
package com.arms.requirement.reqadd.service;

import com.arms.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.globaltreemap.service.GlobalTreeMapService;
import com.arms.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.jira.jiraproject.model.JiraProjectEntity;
import com.arms.jira.jiraproject.service.JiraProject;
import com.arms.jira.jiraproject.service.JiraProjectImpl;
import com.arms.jira.jiraserver.model.JiraServerEntity;
import com.arms.product_service.pdservice.model.PdServiceEntity;
import com.arms.requirement.reqadd.model.ReqAddEntity;
import com.arms.util.external_communicate.dto.cloud.FieldsDTO;
import com.arms.util.external_communicate.dto.onpremise.OnPremiseJiraIssueInputDTO;
import com.arms.util.external_communicate.엔진통신기;
import com.arms.util.external_communicate.dto.cloud.CloudJiraIssueInputDTO;
import com.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


@AllArgsConstructor
@Service("reqAdd")
public class ReqAddImpl extends TreeServiceImpl implements ReqAdd{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private com.arms.util.external_communicate.엔진통신기 엔진통신기;

	@Autowired
	private GlobalTreeMapService globalTreeMapService;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Override
	@Transactional
	public ReqAddEntity addReqNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		SessionUtil.setAttribute("addNode",changeReqTableName);

		ReqAddEntity savedReqAddEntity = this.addNode(reqAddEntity);

		SessionUtil.removeAttribute("addNode");


		Long 추가된_요구사항의_아이디 = savedReqAddEntity.getC_id();
		PdServiceEntity 추가된_요구사항의_제품서비스 = savedReqAddEntity.getPdServiceEntity();
		String 추가된_요구사항의_제품서비스_버전리스트 = savedReqAddEntity.getC_req_pdservice_versionset_link();

		ObjectMapper objectMapper = new ObjectMapper();
		List<String> 디비에저장된_제품서비스_하위의_버전리스트 = Arrays.asList(objectMapper.readValue(추가된_요구사항의_제품서비스_버전리스트, String[].class));

		List<GlobalTreeMapEntity> 제품서비스_버전에_연결된정보들 = new ArrayList<GlobalTreeMapEntity>();

		for( String 디비에저장된_제품서비스하위_버전 : 디비에저장된_제품서비스_하위의_버전리스트 ){
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setPdservice_link(추가된_요구사항의_제품서비스.getC_id());
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			제품서비스_버전에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);
		}

		for( GlobalTreeMapEntity 연결정보 : 제품서비스_버전에_연결된정보들 ){

			if( 연결정보.getJiraproject_link() != null ){

				Long 연결된_제품서비스_아이디 = 연결정보.getPdservice_link();
				Long 연결된_제품서비스_버전_아이디 = 연결정보.getPdserviceversion_link();

				Long 제품서비스_버전에_연결된_지라프로젝트_아이디 = 연결정보.getJiraproject_link();
				JiraProjectEntity 지라프로젝트_검색용_엔티티 = new JiraProjectEntity();
				지라프로젝트_검색용_엔티티.setC_id(제품서비스_버전에_연결된_지라프로젝트_아이디);
				JiraProjectEntity 검색된_지라프로젝트 = jiraProject.getNode(지라프로젝트_검색용_엔티티);

				JiraServerEntity 검색된_지라서버 = 검색된_지라프로젝트.getJiraServerEntity();
				Long 연결된_지라서버_아이디 = 검색된_지라서버.getC_id();
				String 지라서버_커넥트아이디 = 검색된_지라서버.getC_jira_server_connect_id();
				String 지라서버_타입 = 검색된_지라서버.getC_jira_server_type();
				
				Set<JiraIssuePriorityEntity> 지라서버_이슈우선순위_리스트 = 검색된_지라서버.getJiraIssuePriorityEntities();
				JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 지라서버_이슈우선순위_리스트.stream()
						.filter(entity -> entity.getC_desc().equals("req"))
						.findFirst()
						.orElse(null);

				Set<JiraIssueResolutionEntity> 지라서버_이슈해결책_리스트 = 검색된_지라서버.getJiraIssueResolutionEntities();
				JiraIssueResolutionEntity 요구사항_이슈_해결책 = 지라서버_이슈해결책_리스트.stream()
						.filter(entity -> entity.getC_desc().equals("req"))
						.findFirst()
						.orElse(null);

				Set<JiraIssueStatusEntity> 지라서버_이슈상태_리스트 = 검색된_지라서버.getJiraIssueStatusEntities();
				JiraIssueStatusEntity 요구사항_이슈_상태 = 지라서버_이슈상태_리스트.stream()
						.filter(entity -> entity.getC_desc().equals("req"))
						.findFirst()
						.orElse(null);

				Set<JiraIssueTypeEntity> 지라서버_이슈타입_리스트 = 검색된_지라서버.getJiraIssueTypeEntities();
				JiraIssueTypeEntity 요구사항_이슈_타입 = 지라서버_이슈타입_리스트.stream()
						.filter(entity -> entity.getC_desc().equals("req"))
						.findFirst()
						.orElse(null);

				//준비된 파라미터.
				logger.info("추가된_요구사항의_제품서비스 = " + 추가된_요구사항의_제품서비스.getC_title());

				logger.info("연결된_제품서비스_아이디 = " + 연결된_제품서비스_아이디);
				logger.info("연결된_제품서비스_버전_아이디 = " + 연결된_제품서비스_버전_아이디);

				logger.info("검색된_지라서버 = " + 검색된_지라서버.getC_title());
				logger.info("검색된_지라프로젝트 = " + 검색된_지라프로젝트.getC_title());

				logger.info("요구사항_이슈_우선순위 = " + 요구사항_이슈_우선순위.getC_title());
				logger.info("요구사항_이슈_해결책 = " + 요구사항_이슈_해결책.getC_title());
				logger.info("요구사항_이슈_상태 = " + 요구사항_이슈_상태.getC_title());
				logger.info("요구사항_이슈_타입 = " + 요구사항_이슈_타입.getC_title());

				logger.info("요구사항_이슈_내용 요구사항아이디 링크 URL = " + 추가된_요구사항의_아이디);
			}

		}


		//이슈 등록하고
		//등록된 이슈를 요구사항과 연결해 줘야 함.

		//changeReqTableName 으로 숫자값만 가져와서
		//어떤 제품(서비스)인지 확인하고
		//StringUtility

		//JIRA 연결 정보를 가져와서
		//이슈가 이미 있는지 확인? <- 이게 필요할까?
		//각 연결정보의 프로젝트에 이슈를 생성한다.

		return savedReqAddEntity;
	}

	@Override
	@Transactional
	public ReqAddEntity moveReqNode(ReqAddEntity reqAddEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("moveNode",changeReqTableName);

		ReqAddEntity savedReqAddEntity = this.moveNode(reqAddEntity, request);

		SessionUtil.removeAttribute("moveNode");

		return savedReqAddEntity;
	}
}