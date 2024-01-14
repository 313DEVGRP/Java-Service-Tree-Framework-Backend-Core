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
package com.arms.api.requirement.reqadd.service;

import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.util.external_communicate.dto.*;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import java.util.*;


@AllArgsConstructor
@Service("reqAdd")
public class ReqAddImpl extends TreeServiceImpl implements ReqAdd{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private com.arms.api.util.external_communicate.엔진통신기 엔진통신기;

	@Autowired
	private com.arms.api.util.external_communicate.내부통신기 내부통신기;

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

	@Autowired
	private ArmsDetailUrlConfig armsDetailUrlConfig;

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
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			제품서비스_버전에_연결된정보들.addAll(globalTreeMapService.findAllBy(globalTreeMap)); // 버전또한 유니크하니까, 해당 버전에 연결된 지라프로젝트들 가져온다.
		}

		Long 제품서비스_아이디 = 추가된_요구사항의_제품서비스.getC_id();
		for( GlobalTreeMapEntity 연결정보 : 제품서비스_버전에_연결된정보들 ){

			if( 연결정보.getJiraproject_link() != null ){

				Long 제품서비스_버전_아이디 = 연결정보.getPdserviceversion_link();
				Long 지라_프로젝트_아이디 = 연결정보.getJiraproject_link();

				PdServiceVersionEntity 제품서비스_버전_검색전용 = new PdServiceVersionEntity();
				제품서비스_버전_검색전용.setC_id(제품서비스_버전_아이디);
				PdServiceVersionEntity 제품서비스_버전 = pdServiceVersion.getNode(제품서비스_버전_검색전용);

				GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
				globalTreeMap.setJiraproject_link(지라_프로젝트_아이디);
				List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

				GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
						.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null) // 특정값이 null이 아닌 엔티티들로 필터링
						.findFirst() // 첫 번째로 찾은 엔티티를 반환 (단일 값)
						.orElse(null); // 만약 찾은 엔티티가 없으면 null 반환

				Long 지라서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();

				//logger.info("제품 서비스 링크 = " + 제품서비스_아이디);
				logger.info("제품 서비스 버전 링크 = " + 제품서비스_버전_아이디);
				logger.info("지라 서버 링크 = " + 지라서버_아이디);
				logger.info("지라 프로젝트 링크 = " + 지라_프로젝트_아이디);

				JiraServerEntity 지라서버_검색용_엔티티 = new JiraServerEntity();
				지라서버_검색용_엔티티.setC_id(지라서버_아이디);
				JiraServerEntity 검색된_지라서버 = jiraServer.getNode(지라서버_검색용_엔티티);
				
				JiraProjectEntity 지라프로젝트_검색용_엔티티 = new JiraProjectEntity();
				지라프로젝트_검색용_엔티티.setC_id(지라_프로젝트_아이디);
				JiraProjectEntity 검색된_지라프로젝트 = jiraProject.getNode(지라프로젝트_검색용_엔티티);

				Set<JiraIssuePriorityEntity> 지라서버_이슈우선순위_리스트 = 검색된_지라서버.getJiraIssuePriorityEntities();
				JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 지라서버_이슈우선순위_리스트.stream()
						.filter(우선순위 -> StringUtils.equals(우선순위.getC_check(),"true"))
						.findFirst().orElse(null);

				Set<JiraIssueResolutionEntity> 지라서버_이슈해결책_리스트 = 검색된_지라서버.getJiraIssueResolutionEntities();
				JiraIssueResolutionEntity 요구사항_이슈_해결책 = 지라서버_이슈해결책_리스트.stream()
						.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
						.findFirst().orElse(null);

				JiraIssueTypeEntity 요구사항_이슈_타입 = new JiraIssueTypeEntity();
				JiraIssueStatusEntity 요구사항_이슈_상태 = new JiraIssueStatusEntity();
				if( 검색된_지라서버.getC_jira_server_type().equals("클라우드")){

					Set<JiraIssueTypeEntity> 클라우드_지라서버_이슈타입_리스트 = 검색된_지라프로젝트.getJiraIssueTypeEntities();
						요구사항_이슈_타입 = 클라우드_지라서버_이슈타입_리스트.stream()
								.filter(이슈타입 -> StringUtils.equals(이슈타입.getC_check(),"true"))
								.findFirst().orElse(null);
					if (요구사항_이슈_타입 == null) {
						// 기본값은 아니지만 arms-requirement 가 있을경우, arms-requirement 를 이슈 유형으로 세팅
						요구사항_이슈_타입 = 클라우드_지라서버_이슈타입_리스트.stream()
								.filter(이슈타입 -> StringUtils.equals(이슈타입.getC_issue_type_name(), "arms-requirement"))
								.findFirst().orElse(null);
					}


					Set<JiraIssueStatusEntity> 클라우드_지라서버_이슈상태_리스트 = 검색된_지라프로젝트.getJiraIssueStatusEntities();
						요구사항_이슈_상태 = 클라우드_지라서버_이슈상태_리스트.stream()
							.filter(이슈상태 -> StringUtils.equals(이슈상태.getC_check(),"true"))
							.findFirst()
							.orElse(null);

				} else if( 검색된_지라서버.getC_jira_server_type().equals("온프레미스")){
					Set<JiraIssueTypeEntity> 지라서버_이슈타입_리스트 = 검색된_지라서버.getJiraIssueTypeEntities();
						요구사항_이슈_타입 = 지라서버_이슈타입_리스트.stream()
								.filter(이슈타입 -> StringUtils.equals(이슈타입.getC_check(),"true"))
								.findFirst().orElse(null);
						if (요구사항_이슈_타입 == null) {
							요구사항_이슈_타입 = 지라서버_이슈타입_리스트.stream()
								.filter(이슈타입 -> StringUtils.equals(이슈타입.getC_issue_type_name(), "arms-requirement"))
								.findFirst().orElse(null);
						}

					Set<JiraIssueStatusEntity> 지라서버_이슈상태_리스트 = 검색된_지라서버.getJiraIssueStatusEntities();
					요구사항_이슈_상태 = 지라서버_이슈상태_리스트.stream()
							.filter(이슈상태 -> StringUtils.equals(이슈상태.getC_check(), "true"))
							.findFirst()
							.orElse(null);
				}else {
					logger.info("지라 서버 타입에 알 수 없는 값이 들어있습니다. :: " + 검색된_지라서버.getC_jira_server_type());
					throw new RuntimeException("unknown jira server type :: " + 검색된_지라서버.getC_jira_server_type());
				}

				//준비된 파라미터.
				logger.info("추가된_요구사항의_제품서비스 = " + 추가된_요구사항의_제품서비스.getC_title());

				logger.info("제품서비스_아이디 = " + 제품서비스_아이디);
				logger.info("제품서비스_버전_아이디 = " + 제품서비스_버전_아이디);

				logger.info("검색된_지라서버 = " + 검색된_지라서버.getC_jira_server_base_url());
				logger.info("검색된_지라프로젝트 = " + 검색된_지라프로젝트.getC_jira_name());

//				logger.info("요구사항_이슈_상태 = " + 요구사항_이슈_상태.getC_issue_status_name());
				if(요구사항_이슈_타입 == null) {
					logger.error("요구사항_이슈_타입이 없습니다.");
				} else {
					logger.info("요구사항_이슈_타입 = " + 요구사항_이슈_타입.getC_issue_type_name());
				}

				if (요구사항_이슈_해결책 == null) {
					logger.info("요구사항_이슈_해결책 기본값이 없습니다. 요구사항은 등록됩니다.");
				} else {
					logger.info("요구사항_이슈_해결책 = " + 요구사항_이슈_해결책.getC_issue_resolution_name());
				}

				logger.info("요구사항_이슈_내용 요구사항아이디 링크 URL = " + 추가된_요구사항의_아이디);

				지라이슈필드_데이터.프로젝트 프로젝트 = 지라이슈필드_데이터.프로젝트.builder().id(검색된_지라프로젝트.getC_desc())
						.key(검색된_지라프로젝트.getC_jira_key())
						.name(검색된_지라프로젝트.getC_jira_name())
						.self(검색된_지라프로젝트.getC_jira_url())
						.build();

				지라이슈유형_데이터 유형 = new 지라이슈유형_데이터();
				유형.setId(요구사항_이슈_타입.getC_issue_type_id());
				유형.setName(요구사항_이슈_타입.getC_issue_type_name());
				유형.setSelf(요구사항_이슈_타입.getC_issue_type_url());

				지라이슈필드_데이터.보고자 암스서버보고자 = new 지라이슈필드_데이터.보고자();
				암스서버보고자.setName(검색된_지라서버.getC_jira_server_connect_id());
				암스서버보고자.setEmailAddress("313cokr@gmail.com");

				지라이슈필드_데이터.담당자 암스서버담당자 = new 지라이슈필드_데이터.담당자();
				암스서버담당자.setName(검색된_지라서버.getC_jira_server_connect_id());
				암스서버담당자.setEmailAddress("313cokr@gmail.com");

				String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
						"✔ 본 이슈는 자동으로 관리되므로,\n" +
						"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
						"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
						"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
						"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
						"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
						"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
						"자세한 요구사항 내용 확인 ⇒ http://" + armsDetailUrlConfig.getAddress() + "/arms/detail.html?page=detail&pdService=" + 제품서비스_아이디 +
						"&pdServiceVersion=" + 제품서비스_버전_아이디 + "&reqAdd=" + 추가된_요구사항의_아이디 +
						"&jiraServer=" + 지라서버_아이디 + "&jiraProject=" + 지라_프로젝트_아이디 + "\n" +
						"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n\n" +
						"※ 『 아래는 입력된 요구사항 내용입니다. 』\n\n\n";

				이슈내용 = 이슈내용 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean(savedReqAddEntity.getC_req_contents(), Whitelist.none())),"&nbsp;", " ");


				지라이슈필드_데이터 요구사항이슈_필드;

				if (요구사항_이슈_우선순위 == null) {
					logger.info("요구사항_이슈_우선순위 기본값이 없습니다. 요구사항은 등록됩니다.");
					 요구사항이슈_필드 = 지라이슈필드_데이터
							.builder()
							.project(프로젝트)
							.issuetype(유형)
							.summary(savedReqAddEntity.getC_title())
							.description(이슈내용)
							.build();
				} else {
					logger.info("요구사항_이슈_우선순위 = " + 요구사항_이슈_우선순위.getC_issue_priority_name());
					지라이슈우선순위_데이터 우선순위 = new 지라이슈우선순위_데이터();
					우선순위.setName(요구사항_이슈_우선순위.getC_issue_priority_name());
					우선순위.setSelf(요구사항_이슈_우선순위.getC_issue_priority_url());
					우선순위.setId(요구사항_이슈_우선순위.getC_issue_priority_id());

					 요구사항이슈_필드 = 지라이슈필드_데이터
							.builder()
							.project(프로젝트)
							.issuetype(유형)
							.priority(우선순위)
							.summary(savedReqAddEntity.getC_title())
							.description(이슈내용)
							.build();
				}


				지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
																.builder()
																.fields(요구사항이슈_필드)
																.build();

				logger.info("ReqAddImpl = engine parameter :: " + objectMapper.writeValueAsString(요구사항_이슈));

				지라이슈_데이터 생성된_요구사항_이슈 = 엔진통신기.이슈_생성하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 요구사항_이슈);

				ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
				reqStatusDTO.setRef(TreeConstant.First_Node_CID);
				reqStatusDTO.setC_type(TreeConstant.Leaf_Node_TYPE);
				reqStatusDTO.setC_title(savedReqAddEntity.getC_title());
				//-- 제품 서비스
				reqStatusDTO.setC_pdservice_link(추가된_요구사항의_제품서비스.getC_id());
				reqStatusDTO.setC_pdservice_name(추가된_요구사항의_제품서비스.getC_title());
				//-- 제품 서비스 버전
				reqStatusDTO.setC_pds_version_link(제품서비스_버전.getC_id());
				reqStatusDTO.setC_pds_version_name(제품서비스_버전.getC_title());
				//-- 제품 서비스 연결 지라 server
				reqStatusDTO.setC_jira_server_link(검색된_지라서버.getC_id());
				reqStatusDTO.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
				reqStatusDTO.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());
				//-- 제품 서비스 연결 지라 프로젝트
				reqStatusDTO.setC_jira_project_link(검색된_지라프로젝트.getC_id());
				reqStatusDTO.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
				reqStatusDTO.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
				reqStatusDTO.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());


				//-- 요구사항
				reqStatusDTO.setC_req_link(savedReqAddEntity.getC_id());
				reqStatusDTO.setC_req_name(savedReqAddEntity.getC_title());

				//-- 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든.
				reqStatusDTO.setC_issue_key(생성된_요구사항_이슈.getKey());
				reqStatusDTO.setC_issue_url(생성된_요구사항_이슈.getSelf());

				//-- 이슈 우선순위 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
				if(요구사항_이슈_우선순위 != null) {
					// null이 아닐때만 statusDTO에 우선순위 값 넘긴다.
					reqStatusDTO.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				//-- 이슈 상태 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
//				reqStatusDTO.setC_issue_status_link(요구사항_이슈_상태.getC_id());
//				reqStatusDTO.setC_issue_status_name(요구사항_이슈_상태.getC_issue_status_name());

				//-- 이슈 해결책 ( 요구사항 자산의 이슈 이든, 아니면 연결된 이슈이든 )
//				reqStatusDTO.setC_issue_resolution_link(요구사항_이슈_해결책.getC_id());
//				reqStatusDTO.setC_issue_resolution_name(요구사항_이슈_해결책.getC_issue_resolution_name());

				reqStatusDTO.setC_issue_reporter(암스서버보고자.getName());
				reqStatusDTO.setC_issue_assignee(암스서버담당자.getName());

				reqStatusDTO.setC_issue_create_date(new Date());

				logger.info("ReqAddImpl = reqStatusDTO :: " + objectMapper.writeValueAsString(reqStatusDTO));

				ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_저장하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
				if(결과.getStatusCode().is2xxSuccessful()){
					chat.sendMessageByEngine("요구사항 이슈가 생성 후, 지라서버에 등록되었습니다.");
				}

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

	@Override
	public ReqAddDetailDTO getDetail(FollowReqLinkDTO followReqLinkDTO, String changeReqTableName) throws Exception {

		Long targetTableId = followReqLinkDTO.getPdService();

		String targetReqAddTableName = changeReqTableName + targetTableId;

		SessionUtil.setAttribute("getDetail",targetReqAddTableName);
		ReqAddEntity searchReqAddEntity = new ReqAddEntity();
		searchReqAddEntity.setC_id(followReqLinkDTO.getReqAdd());
		ReqAddEntity reqAddEntity = this.getNode(searchReqAddEntity);
		SessionUtil.removeAttribute("getDetail");

		PdServiceEntity pdServiceEntity = reqAddEntity.getPdServiceEntity();

		PdServiceVersionEntity pdServiceVersionEntity = pdServiceEntity.getPdServiceVersionEntities()
			.stream()
			.filter(a -> followReqLinkDTO.getPdServiceVersion().equals(a.getC_id()))
			.findFirst().orElseGet(() -> new PdServiceVersionEntity());

		if(followReqLinkDTO.getJiraProject()!=null){
			JiraProjectEntity jiraProjectSearchEntity = new JiraProjectEntity();
			jiraProjectSearchEntity.setC_id(followReqLinkDTO.getJiraProject());
			JiraProjectEntity jiraProjectEntity = jiraProject.getNode(jiraProjectSearchEntity);
		}

		if(followReqLinkDTO.getJiraServer()!=null) {
			JiraServerEntity searchJiraServerEntity = new JiraServerEntity();
			searchJiraServerEntity.setC_id(followReqLinkDTO.getJiraServer());
			JiraServerEntity jiraServerEntity = jiraServer.getNode(searchJiraServerEntity);
		}


		return ReqAddDetailDTO.builder()
			.pdService_c_title(pdServiceEntity.getC_title())
			.pdServiceVersion_c_title(pdServiceVersionEntity.getC_title())
			.pdService_c_id(pdServiceEntity.getC_id())
			.reqAdd_c_title(reqAddEntity.getC_title())
			.reqAdd_c_req_writer(reqAddEntity.getC_req_writer())
			.reqAdd_c_req_create_date(reqAddEntity.getC_req_create_date())
			.reqAdd_c_req_reviewer01(reqAddEntity.getC_req_reviewer01())
			.reqAdd_c_req_reviewer02(reqAddEntity.getC_req_reviewer02())
			.reqAdd_c_req_reviewer03(reqAddEntity.getC_req_reviewer03())
			.reqAdd_c_req_reviewer04(reqAddEntity.getC_req_reviewer04())
			.reqAdd_c_req_reviewer05(reqAddEntity.getC_req_reviewer05())
			.reqAdd_c_req_contents(reqAddEntity.getC_req_contents())
			.build();
	}


}
