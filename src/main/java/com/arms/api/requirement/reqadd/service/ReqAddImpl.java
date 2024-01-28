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
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.api.requirement.reqadd.model.JiraServerType;
import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqdifficulty.model.ReqDifficultyEntity;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqstate.model.ReqStateEntity;
import com.arms.api.requirement.reqdifficulty.service.ReqDifficulty;
import com.arms.api.requirement.reqpriority.model.ReqPriorityEntity;
import com.arms.api.requirement.reqpriority.service.ReqPriority;
import com.arms.api.requirement.reqstate.service.ReqState;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
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
import java.util.stream.Collectors;


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

	@Autowired
	private PdService pdService;

	//새로 작성중인 요구사항 생성 - 중간 작성.
	@Override
	@Transactional
	public ReqAddEntity addReqNodeNew(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		SessionUtil.setAttribute("addNode",changeReqTableName);

		ReqAddEntity savedReqAddEntity = this.addNode(reqAddEntity);

		SessionUtil.removeAttribute("addNode");


		Long 추가된_요구사항의_아이디 = savedReqAddEntity.getC_id();
		PdServiceEntity 추가된_요구사항의_제품서비스 = savedReqAddEntity.getPdServiceEntity();
		String 추가된_요구사항의_제품서비스_버전리스트 = savedReqAddEntity.getC_req_pdservice_versionset_link();

		ObjectMapper objectMapper = new ObjectMapper();
		List<String> 디비에저장된_제품서비스_하위의_버전리스트 = Arrays.asList(objectMapper.readValue(추가된_요구사항의_제품서비스_버전리스트, String[].class));

		Long 제품서비스_아이디 = 추가된_요구사항의_제품서비스.getC_id();
		Set<PdServiceVersionEntity> 제품서비스_버전_세트 = 추가된_요구사항의_제품서비스.getPdServiceVersionEntities();
		Map<Long, String> 제품_버전아이디_버전명_맵 = 제품서비스_버전_세트.stream().collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));

		Map<Long,Set<Long>> 지라프로젝트_버전아이디_맵 = new HashMap<>();

		// 지라프로젝트 기준으로 버전 Set 생성
		for( String 디비에저장된_제품서비스하위_버전 : 디비에저장된_제품서비스_하위의_버전리스트 ){
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			List<GlobalTreeMapEntity> 버전_지라프로젝트_목록 = globalTreeMapService.findAllBy(globalTreeMap).stream()
					.filter(엔티티 -> 엔티티.getJiraproject_link() != null).collect(Collectors.toList());

			for (GlobalTreeMapEntity 엔티티 : 버전_지라프로젝트_목록) {
				Long 지라프로젝트_아이디 = 엔티티.getJiraproject_link();
				Long 버전_아이디 = 엔티티.getPdserviceversion_link();

				if(지라프로젝트_버전아이디_맵.containsKey(지라프로젝트_아이디)) {
					지라프로젝트_버전아이디_맵.get(지라프로젝트_아이디).add(버전_아이디);
				} else {
					Set<Long> 버전_셋 = new HashSet<>();
					버전_셋.add(버전_아이디);
					지라프로젝트_버전아이디_맵.put(지라프로젝트_아이디, 버전_셋);
				}
			}
		}

		List<Long> 지라프로젝트_아이디_목록 = 지라프로젝트_버전아이디_맵.keySet().stream().collect(Collectors.toList());

		// 각 지라프로젝트를 활용해서 요구사항 만들기.
		for ( Long 지라프로젝트_아이디 : 지라프로젝트_아이디_목록 ) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setJiraproject_link(지라프로젝트_아이디);
			List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

			GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
					.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null) // 특정값이 null이 아닌 엔티티들로 필터링
					.findFirst() // 첫 번째로 찾은 엔티티를 반환 (단일 값)
					.orElse(null); // 만약 찾은 엔티티가 없으면 null 반환

			Long 지라서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();

			logger.info("지라 서버 링크 = " + 지라서버_아이디);

			JiraServerEntity 지라서버_검색용_엔티티 = new JiraServerEntity();
			지라서버_검색용_엔티티.setC_id(지라서버_아이디);
			JiraServerEntity 검색된_지라서버 = jiraServer.getNode(지라서버_검색용_엔티티);

			JiraProjectEntity 지라프로젝트_검색용_엔티티 = new JiraProjectEntity();
			지라프로젝트_검색용_엔티티.setC_id(지라프로젝트_아이디);
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

			// 준비된 파라미터
			logger.info("추가된_요구사항의_제품서비스 = " + 추가된_요구사항의_제품서비스.getC_title());
			logger.info("제품서비스_아이디 = " + 제품서비스_아이디);

			Set<Long> 버전아이디_세트 = 지라프로젝트_버전아이디_맵.get(지라프로젝트_아이디);
			List<Long> 버전아이디_내림차순_목록 = 버전아이디_세트.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

			String 버전아이디_내림차순_문자열 = 버전아이디_내림차순_목록.stream().map(String::valueOf)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));
			String 버전명_내림차순_문자열 = 버전아이디_내림차순_목록.stream().map(제품_버전아이디_버전명_맵::get)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			logger.info("요구사항_매핑_버전_아이디_목록 = {}", 버전아이디_내림차순_문자열);
			logger.info("요구사항_매핑_버전_이름_목록 = {}", 버전명_내림차순_문자열);

			logger.info("검색된_지라서버 = " + 검색된_지라서버.getC_jira_server_base_url());
			logger.info("검색된_지라프로젝트 = " + 검색된_지라프로젝트.getC_jira_name());

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
					"&reqAdd=" + 추가된_요구사항의_아이디 + "&jiraServer=" + 지라서버_아이디 + "&jiraProject=" + 지라프로젝트_아이디 + "\n" +
					//"&pdServiceVersion=" + 제품서비스_버전_아이디 + "&reqAdd=" + 추가된_요구사항의_아이디 +
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

			//-- 지라프로젝트에 생성된 요구사항의 매핑버전목록
			reqStatusDTO.setC_pds_version_name(버전명_내림차순_문자열);
			reqStatusDTO.setC_req_pdservice_versionset_link(버전아이디_내림차순_문자열);

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

			//-- 요구사항 우선순위
			reqStatusDTO.setC_req_priority_link(savedReqAddEntity.getReqPriorityEntity().getC_id());
			reqStatusDTO.setC_req_priority_name(savedReqAddEntity.getReqPriorityEntity().getC_title());
			//-- 요구사항 상태
			reqStatusDTO.setC_req_state_link(savedReqAddEntity.getReqStateEntity().getC_id());
			reqStatusDTO.setC_req_state_name(savedReqAddEntity.getReqStateEntity().getC_title());
			//-- 요구사항 난이도
			reqStatusDTO.setC_req_difficulty_link(savedReqAddEntity.getReqDifficultyEntity().getC_id());
			reqStatusDTO.setC_req_difficulty_name(savedReqAddEntity.getReqDifficultyEntity().getC_title());

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

				//-- 요구사항 우선순위
				reqStatusDTO.setC_req_priority_link(savedReqAddEntity.getReqPriorityEntity().getC_id());
				reqStatusDTO.setC_req_priority_name(savedReqAddEntity.getReqPriorityEntity().getC_title());
				//-- 요구사항 상태
				reqStatusDTO.setC_req_state_link(savedReqAddEntity.getReqStateEntity().getC_id());
				reqStatusDTO.setC_req_state_name(savedReqAddEntity.getReqStateEntity().getC_title());
				//-- 요구사항 난이도
				reqStatusDTO.setC_req_difficulty_link(savedReqAddEntity.getReqDifficultyEntity().getC_id());
				reqStatusDTO.setC_req_difficulty_name(savedReqAddEntity.getReqDifficultyEntity().getC_title());

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

	@Override
	@Transactional
	public Integer updateReqNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {
		logger.info("ReqAddImpl :: updateReqNode");
		// 1. 수정 전 ReqAdd 조회
		ResponseEntity<LoadReqAddDTO> 요구사항조회 = 내부통신기.요구사항조회(changeReqTableName, reqAddEntity.getC_id());
		logger.info("ReqAddImpl :: updateReqNode :: 응답 성공");
		LoadReqAddDTO loadReqAddDTO = 요구사항조회.getBody();
		logger.info("ReqAddImpl :: updateReqNode :: 요구사항조회 :: " + loadReqAddDTO.toString());
		String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", ""); // ex) 22

		// 2. 수정 전 후 비교
		ObjectMapper objectMapper = new ObjectMapper();

		Set<String> 수정전버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
		Set<String> 현재버전셋 = objectMapper.readValue(reqAddEntity.getC_req_pdservice_versionset_link(), Set.class);
		Set<String> 루프용버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
		루프용버전셋.addAll(현재버전셋);
		logger.info("ReqAddImpl :: updateReqNode :: 수정 전 버전 -> " + 수정전버전셋);
		logger.info("ReqAddImpl :: updateReqNode :: 수정 후 버전 -> " + 현재버전셋);

		Set<String> 유지된버전 = 유지된버전찾기(수정전버전셋, 현재버전셋);
		Set<String> 추가된버전 = 추가된버전찾기(수정전버전셋, 현재버전셋);
		Set<String> 삭제된버전 = 삭제된버전찾기(수정전버전셋, 현재버전셋);

		logger.info("ReqAddImpl :: updateReqNode :: 유지된버전 -> " + 유지된버전);
		logger.info("ReqAddImpl :: updateReqNode :: 추가된버전 -> " + 추가된버전);
		logger.info("ReqAddImpl :: updateReqNode :: 삭제된버전 -> " + 삭제된버전);

		String 수정전제목 = loadReqAddDTO.getC_title();
		String 현재제목 = reqAddEntity.getC_title();
		logger.info("ReqAddImpl :: updateReqNode :: 수정 전 제목 -> " + 수정전제목);
		logger.info("ReqAddImpl :: updateReqNode :: 수정 후 제목 -> " + 현재제목);

		String 수정전본문 = loadReqAddDTO.getC_req_contents();
		String 현재본문 = reqAddEntity.getC_req_contents();
		logger.info("ReqAddImpl :: updateReqNode :: 수정 전 본문 -> " + 수정전본문);
		logger.info("ReqAddImpl :: updateReqNode :: 수정 후 본문 -> " + 현재본문);

		String 요구사항최초요청자 = loadReqAddDTO.getC_req_writer();

		logger.info("ReqAddImpl :: updateReqNode :: 요구사항최초요청자 -> " + 요구사항최초요청자);

		List<Long> 현재버전셋리스트 = 현재버전셋.stream().map(Long::valueOf).collect(Collectors.toList());
		List<Long> 루프용버전셋리스트 = 루프용버전셋.stream().map(Long::valueOf).collect(Collectors.toList());
		logger.info("ReqAddImpl :: updateReqNode :: 현재버전셋리스트 -> " + 현재버전셋리스트);

		List<PdServiceVersionEntity> 수정될버전데이터 = pdServiceVersion.getVersionListByCids(현재버전셋리스트);

		logger.info("ReqAddImpl :: updateReqNode :: 수정될버전데이터 -> " + 수정될버전데이터);

		String 버전명목록 = 수정될버전데이터.stream().map(PdServiceVersionEntity::getC_title).collect(Collectors.joining("\",\"", "[\"", "\"]"));

		logger.info("ReqAddImpl :: updateReqNode :: 버전명목록 -> " + 버전명목록);

		PdServiceEntity pdServiceEntity = new PdServiceEntity();
		pdServiceEntity.setC_id(Long.valueOf(pdServiceId));
		PdServiceEntity 제품데이터 = pdService.getNode(pdServiceEntity);
		Set<PdServiceVersionEntity> pdServiceVersionEntities = 제품데이터.getPdServiceVersionEntities();
		String 제품명 = 제품데이터.getC_title();

		logger.info("ReqAddImpl :: updateReqNode :: 제품명 -> " + 제품명);

		// 3. ReqAdd 업데이트
        SessionUtil.setAttribute("updateNode", changeReqTableName);
		this.updateNode(reqAddEntity);
        SessionUtil.removeAttribute("updateNode");

		logger.info("ReqAddImpl :: updateReqNode :: ReqAdd 업데이트 완료");

		List<GlobalTreeMapEntity> 글로벌트리맵By버전 = globalTreeMapService.findAllByIds(루프용버전셋리스트, "pdserviceversion_link")
				.stream()
				.filter(globalTreeMap -> globalTreeMap.getJiraproject_link() != null)
				.collect(Collectors.toList());

		logger.info("ReqAddImpl :: updateReqNode :: 글로벌트리맵By버전 -> " + 글로벌트리맵By버전);

		List<ReqStatusEntity> reqStatusEntityList = 내부통신기.reqStatusList("T_ARMS_REQSTATUS_"+pdServiceId, new ReqStatusDTO());

		String 일반지라이슈본문 = 등록및수정지라이슈본문가져오기(reqAddEntity, 요구사항최초요청자, 제품명, 버전명목록);
		String 삭제지라이슈본문 = 삭제할지라이슈본문가져오기();

		logger.info("ReqAddImpl :: updateReqNode :: 일반지라이슈본문 -> " + 일반지라이슈본문);
		logger.info("ReqAddImpl :: updateReqNode :: 삭제지라이슈본문 -> " + 삭제지라이슈본문);

		// 4. 버전 변경에 대한 처리. 지라 이슈(추가, 삭제, 변경), ReqStatus 처리
		for (GlobalTreeMapEntity globalTreeMap : 글로벌트리맵By버전) {

			Long 지라_프로젝트_아이디 = globalTreeMap.getJiraproject_link();
			String 현재버전 = globalTreeMap.getPdserviceversion_link().toString();

			GlobalTreeMapEntity 글로벌트리맵By지라프로젝트 = GlobalTreeMapEntity.builder().jiraproject_link(지라_프로젝트_아이디).build();
			List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(글로벌트리맵By지라프로젝트);

			GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
					.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null)
					.findFirst()
					.orElseThrow(() -> new RuntimeException(""));

			Long 지라서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();

			JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);
			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라_프로젝트_아이디);
			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항이슈우선순위검색(검색된_지라서버);
			JiraIssueResolutionEntity 요구사항_이슈_해결책 = 요구사항이슈해결책검색(검색된_지라서버);
			JiraIssueStatusEntity 요구사항_이슈_상태;
			JiraIssueTypeEntity 요구사항_이슈_타입;

			JiraServerType jiraServerType = JiraServerType.fromString(검색된_지라서버.getC_jira_server_type());

			switch (jiraServerType) {
				case CLOUD:
					요구사항_이슈_상태 = 클라우드요구사항이슈상태검색(검색된_지라프로젝트);
					요구사항_이슈_타입 = 클라우드요구사항이슈타입검색(검색된_지라프로젝트);
					break;
				case ON_PREMISE:
					요구사항_이슈_상태 = 온프레미스요구사항이슈상태검색(검색된_지라서버);
					요구사항_이슈_타입 = 온프레미스요구사항이슈타입검색(검색된_지라서버);
					break;
				default:
					throw new IllegalArgumentException("Invalid Jira Server Type: " + jiraServerType);
			}

			지라이슈필드_데이터.프로젝트 프로젝트 = 지라프로젝트빌더(검색된_지라프로젝트);

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

			if(유지된버전.contains(현재버전)) {
				지라이슈필드_데이터 지라이슈생성데이터 = get지라이슈생성데이터(reqAddEntity, 프로젝트, 유형, 일반지라이슈본문, 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책);

				지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
						.builder()
						.fields(지라이슈생성데이터)
						.build();

				Long 지라서버링크 = 검색된_지라서버.getC_id();
				Long 지라프로젝트링크 = 검색된_지라프로젝트.getC_id();

				ReqStatusEntity 일치하는ReqStatus = reqStatusEntityList.stream()
						.filter(a -> a.getC_jira_server_link().equals(지라서버링크) && a.getC_jira_project_link().equals(지라프로젝트링크))
						.filter(a -> a.getC_req_link().equals(reqAddEntity.getC_id()))
						.findFirst().orElseThrow();

				엔진통신기.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 일치하는ReqStatus.getC_issue_key(), 요구사항_이슈);

				ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
				reqStatusDTO.setRef(TreeConstant.First_Node_CID);
				reqStatusDTO.setC_type(TreeConstant.Leaf_Node_TYPE);

				/* 제품 및 버전*/
				reqStatusDTO.setC_title(현재제목);
				reqStatusDTO.setC_contents(현재본문);
				reqStatusDTO.setC_pdservice_name(제품명);
				reqStatusDTO.setC_pds_version_link(globalTreeMap.getPdserviceversion_link());
				reqStatusDTO.setC_pds_version_name(버전명목록);
				reqStatusDTO.setC_req_pdservice_versionset_link(reqAddEntity.getC_req_pdservice_versionset_link()); // ["33", "35"]

				/* 지라 서버 */
				reqStatusDTO.setC_jira_server_link(검색된_지라서버.getC_id());
				reqStatusDTO.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
				reqStatusDTO.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

				/* 지라 프로젝트 */
				reqStatusDTO.setC_jira_project_link(검색된_지라프로젝트.getC_id());
				reqStatusDTO.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
				reqStatusDTO.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
				reqStatusDTO.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

				//-- 요구사항
				reqStatusDTO.setC_req_link(reqAddEntity.getC_id());
				reqStatusDTO.setC_req_name(reqAddEntity.getC_title());

				// 등록일 경우, 엔진 응답 후 처리
				reqStatusDTO.setC_issue_key(일치하는ReqStatus.getC_issue_key());
				reqStatusDTO.setC_issue_url(일치하는ReqStatus.getC_issue_url());

				//-- 제품 서비스 연결 지라 server
				reqStatusDTO.setC_jira_server_link(검색된_지라서버.getC_id());
				reqStatusDTO.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
				reqStatusDTO.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

				//-- 제품 서비스 연결 지라 프로젝트
				reqStatusDTO.setC_jira_project_link(검색된_지라프로젝트.getC_id());
				reqStatusDTO.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
				reqStatusDTO.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
				reqStatusDTO.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

				if(요구사항_이슈_우선순위 != null) {
					reqStatusDTO.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				if(요구사항_이슈_해결책 != null) {
					reqStatusDTO.setC_issue_resolution_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_resolution_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				if(요구사항_이슈_상태 != null) {
					reqStatusDTO.setC_issue_status_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_status_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				reqStatusDTO.setC_issue_reporter(암스서버보고자.getName());
				reqStatusDTO.setC_issue_assignee(암스서버담당자.getName());
				reqStatusDTO.setC_issue_update_date(new Date());
				reqStatusDTO.setC_id(일치하는ReqStatus.getC_id());

				ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_"+pdServiceId, reqStatusDTO);

				if(결과.getStatusCode().is2xxSuccessful()){
					String 업데이트된버전명 = 제품데이터.getPdServiceVersionEntities().stream()
							.filter(a -> a.getC_id().equals(Long.valueOf(현재버전)))
							.map(PdServiceVersionEntity::getC_title)
							.findFirst().orElseThrow();
					chat.sendMessageByEngine(업데이트된버전명 + " 버전에 연결 된 지라 프로젝트(" + 검색된_지라프로젝트.getC_title() + ")에 지라 이슈가 수정되었습니다.");
				}

			} else if(추가된버전.contains(현재버전)) {
				logger.info("추가된버전 contains 현재버전");
				지라이슈필드_데이터 지라이슈생성데이터 = get지라이슈생성데이터(reqAddEntity, 프로젝트, 유형, 일반지라이슈본문, 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책);

				지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
						.builder()
						.fields(지라이슈생성데이터)
						.build();

				Long 지라서버링크 = 검색된_지라서버.getC_id();
				Long 지라프로젝트링크 = 검색된_지라프로젝트.getC_id();

				logger.info("지라서버링크 :: " + 지라서버링크); // 69
				logger.info("지라프로젝트링크 :: " + 지라프로젝트링크); // 343

				지라이슈_데이터 이슈_생성하기 = 엔진통신기.이슈_생성하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 요구사항_이슈);

				ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
				reqStatusDTO.setRef(TreeConstant.First_Node_CID);
				reqStatusDTO.setC_type(TreeConstant.Leaf_Node_TYPE);

				/* 제품 및 버전*/
				reqStatusDTO.setC_title(현재제목);
				reqStatusDTO.setC_contents(현재본문);
				reqStatusDTO.setC_pdservice_name(제품명);
				reqStatusDTO.setC_pds_version_link(globalTreeMap.getPdserviceversion_link());
				reqStatusDTO.setC_pds_version_name(버전명목록);
				reqStatusDTO.setC_req_pdservice_versionset_link(reqAddEntity.getC_req_pdservice_versionset_link()); // ["33", "35"]

				/* 지라 서버 */
				reqStatusDTO.setC_jira_server_link(검색된_지라서버.getC_id());
				reqStatusDTO.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
				reqStatusDTO.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());
				/* 지라 프로젝트 */
				reqStatusDTO.setC_jira_project_link(검색된_지라프로젝트.getC_id());
				reqStatusDTO.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
				reqStatusDTO.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
				reqStatusDTO.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

				//-- 요구사항
				reqStatusDTO.setC_req_link(reqAddEntity.getC_id());
				reqStatusDTO.setC_req_name(reqAddEntity.getC_title());

				// 등록일 경우, 엔진 응답 후 처리
				reqStatusDTO.setC_issue_key(이슈_생성하기.getKey());
				reqStatusDTO.setC_issue_url(이슈_생성하기.getSelf());

				//-- 제품 서비스 연결 지라 server
				reqStatusDTO.setC_jira_server_link(검색된_지라서버.getC_id());
				reqStatusDTO.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
				reqStatusDTO.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

				//-- 제품 서비스 연결 지라 프로젝트
				reqStatusDTO.setC_jira_project_link(검색된_지라프로젝트.getC_id());
				reqStatusDTO.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
				reqStatusDTO.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
				reqStatusDTO.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

				if(요구사항_이슈_우선순위 != null) {
					reqStatusDTO.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				if(요구사항_이슈_해결책 != null) {
					reqStatusDTO.setC_issue_resolution_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_resolution_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				if(요구사항_이슈_상태 != null) {
					reqStatusDTO.setC_issue_status_link(요구사항_이슈_우선순위.getC_id());
					reqStatusDTO.setC_issue_status_name(요구사항_이슈_우선순위.getC_issue_priority_name());
				}

				reqStatusDTO.setC_issue_reporter(암스서버보고자.getName());
				reqStatusDTO.setC_issue_assignee(암스서버담당자.getName());

				reqStatusDTO.setC_issue_create_date(new Date());

				ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_저장하기("T_ARMS_REQSTATUS_"+pdServiceId, reqStatusDTO);

				if(결과.getStatusCode().is2xxSuccessful()){
					String 업데이트된버전명 = 제품데이터.getPdServiceVersionEntities().stream()
							.filter(a -> a.getC_id().equals(Long.valueOf(현재버전)))
							.map(PdServiceVersionEntity::getC_title)
							.findFirst().orElseThrow();
					chat.sendMessageByEngine(업데이트된버전명 + " 버전에 연결 된 지라 프로젝트(" + 검색된_지라프로젝트.getC_title() + ")에 지라 이슈가 등록되었습니다.");
				}

			} else if(삭제된버전.contains(현재버전)) {
				logger.info("삭제된버전 contains 현재버전");
				지라이슈필드_데이터 지라이슈생성데이터 = get지라이슈생성데이터(reqAddEntity, 프로젝트, 유형, 삭제지라이슈본문 , 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책);

				지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
						.builder()
						.fields(지라이슈생성데이터)
						.build();

				Long 지라서버링크 = 검색된_지라서버.getC_id();
				Long 지라프로젝트링크 = 검색된_지라프로젝트.getC_id();

				ReqStatusEntity 일치하는ReqStatus = reqStatusEntityList.stream()
						.filter(a -> a.getC_jira_server_link().equals(지라서버링크))
						.filter(a -> a.getC_jira_project_link().equals(지라프로젝트링크))
						.filter(a -> a.getC_req_link().equals(reqAddEntity.getC_id()))
						.findFirst().orElseThrow();

				엔진통신기.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 일치하는ReqStatus.getC_issue_key(), 요구사항_이슈);

				ResponseEntity<?> 결과 = 내부통신기.요구사항삭제("T_ARMS_REQSTATUS_"+pdServiceId, 일치하는ReqStatus.getC_id());

				if(결과.getStatusCode().is2xxSuccessful()){
					String 업데이트된버전명 = 제품데이터.getPdServiceVersionEntities().stream()
							.filter(a -> a.getC_id().equals(Long.valueOf(현재버전)))
							.map(PdServiceVersionEntity::getC_title)
							.findFirst().orElseThrow();
					chat.sendMessageByEngine(업데이트된버전명 + " 버전에 연결 된 지라 프로젝트(" + 검색된_지라프로젝트.getC_title() + ")에 등록 된 지라 이슈가 Soft Delete 처리되었습니다. 지라 이슈는 남아있지만, 통계에 수집되지 않습니다.");
				}
			} else {
				throw new RuntimeException("버전 정보가 잘못 되었습니다.");
			}

		}

		return null;
	}

	private 지라이슈필드_데이터 get지라이슈생성데이터(ReqAddEntity reqAddEntity, 지라이슈필드_데이터.프로젝트 프로젝트, 지라이슈유형_데이터 유형, String 지라이슈본문, JiraIssuePriorityEntity 요구사항_이슈_우선순위, JiraIssueStatusEntity 요구사항_이슈_상태, JiraIssueResolutionEntity 요구사항_이슈_해결책) {
		지라이슈필드_데이터 지라이슈생성데이터 = new 지라이슈필드_데이터();
		지라이슈생성데이터.setProject(프로젝트);
		지라이슈생성데이터.setIssuetype(유형);
		지라이슈생성데이터.setSummary(reqAddEntity.getC_title());
		지라이슈생성데이터.setDescription(지라이슈본문);

		if (요구사항_이슈_우선순위 != null) {
			지라이슈우선순위_데이터 우선순위 = new 지라이슈우선순위_데이터();
			우선순위.setName(요구사항_이슈_우선순위.getC_issue_priority_name());
			우선순위.setSelf(요구사항_이슈_우선순위.getC_issue_priority_url());
			우선순위.setId(요구사항_이슈_우선순위.getC_issue_priority_id());
			지라이슈생성데이터.setPriority(우선순위);
		}

		// 엔진에서 오류 발생시킴 if (필드_데이터.getStatus() != null) -> 입력 값에 수정할 수 없는 필드가 있습니다.
//		if (요구사항_이슈_상태 != null) {
//			지라이슈상태_데이터 상태 = new 지라이슈상태_데이터();
//			상태.setId(요구사항_이슈_상태.getC_issue_status_id());
//			상태.setName(요구사항_이슈_상태.getC_issue_status_name());
//			상태.setSelf(요구사항_이슈_상태.getC_issue_status_url());
//			지라이슈생성데이터.setStatus(상태);
//		}

		if (요구사항_이슈_해결책 != null) {
			지라이슈해결책_데이터 해결책 = new 지라이슈해결책_데이터();
			해결책.setId(요구사항_이슈_해결책.getC_issue_resolution_id());
			해결책.setName(요구사항_이슈_해결책.getC_issue_resolution_name());
			해결책.setSelf(요구사항_이슈_해결책.getC_issue_resolution_url());
			지라이슈생성데이터.setResolution(해결책);
		}
		return 지라이슈생성데이터;
	}


	private Set<String> 유지된버전찾기(Set<String> 현재버전, Set<String> 수정할버전) {
		Set<String> 유지된버전 = new HashSet<>(현재버전);
		유지된버전.retainAll(수정할버전);
		return 유지된버전;
	}

	private Set<String> 추가된버전찾기(Set<String> 현재버전, Set<String> 수정할버전) {
		Set<String> 추가된버전 = new HashSet<>(수정할버전);
		추가된버전.removeAll(현재버전);
		return 추가된버전;
	}

	private Set<String> 삭제된버전찾기(Set<String> 현재버전, Set<String> 수정할버전) {
		Set<String> 삭제된버전 = new HashSet<>(현재버전);
		삭제된버전.removeAll(수정할버전);
		return 삭제된버전;
	}

	private 지라이슈필드_데이터.프로젝트 지라프로젝트빌더(JiraProjectEntity 검색된_지라프로젝트) {
		return 지라이슈필드_데이터.프로젝트.builder().id(검색된_지라프로젝트.getC_desc())
				.key(검색된_지라프로젝트.getC_jira_key())
				.name(검색된_지라프로젝트.getC_jira_name())
				.self(검색된_지라프로젝트.getC_jira_url())
				.build();
	}

	private String 등록및수정지라이슈본문가져오기(ReqAddEntity reqAddEntity, String 작성자, String 제품명, String 버전명목록) {
		String 우선순위 = Optional.ofNullable(reqAddEntity.getReqPriorityEntity()).map(ReqPriorityEntity::getC_title).orElse("우선순위");
		String 난이도 = Optional.ofNullable(reqAddEntity.getReqDifficultyEntity()).map(ReqDifficultyEntity::getC_title).orElse("난이도");
		String 상태 = Optional.ofNullable(reqAddEntity.getReqStateEntity()).map(ReqStateEntity::getC_title).orElse("상태");
		String 작성일 = Optional.ofNullable(reqAddEntity.getC_req_create_date()).map(String::valueOf).orElse("최초요청일");
		String 지라이슈본문 = Optional.ofNullable(reqAddEntity.getC_req_contents()).orElse("지라이슈본문");

		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 자동으로 관리되므로,\n" +
				"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
				"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
				"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
				"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
				"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
				"제품 : " + 제품명 + "\n" +
				"제품 버전 : " + 버전명목록 + "\n" +
//				"요구사항 우선순위 : " + 우선순위 + "\n" +
//				"요구사항 난이도 : " + 난이도 + "\n" +
//				"요구사항 상태 : " + 상태 + "\n" +
				"요구사항 작성자 : " + 작성자 + "\n" +
				"요구사항 작성일 : " + 작성일 + "\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n\n" +
				"※ 『 아래는 입력된 요구사항 내용입니다. 』\n\n\n";


		이슈내용 = 이슈내용 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean(지라이슈본문, Whitelist.none())), "&nbsp;", " ");
		return 이슈내용;
	}

	private String 삭제할지라이슈본문가져오기() {
		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 삭제 된 이슈입니다.,\n" +
				"✔ 삭제 된 이슈는 통계에 수집되지 않습니다. \n\n\n";

		이슈내용 = 이슈내용 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean("", Whitelist.none())), "&nbsp;", " ");
		return 이슈내용;
	}

	private JiraIssueTypeEntity 온프레미스요구사항이슈타입검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssueTypeEntity> 지라서버_이슈타입_리스트 = 지라서버.getJiraIssueTypeEntities();
		JiraIssueTypeEntity 요구사항_이슈_타입 = 지라서버_이슈타입_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_타입;
	}

	private JiraIssueStatusEntity 온프레미스요구사항이슈상태검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssueStatusEntity> 지라서버_이슈상태_리스트 = 지라서버.getJiraIssueStatusEntities();
		JiraIssueStatusEntity 요구사항_이슈_상태 = 지라서버_이슈상태_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_상태;
	}

	private JiraIssueTypeEntity 클라우드요구사항이슈타입검색(JiraProjectEntity 지라프로젝트) throws Exception {
		Set<JiraIssueTypeEntity> 지라프로젝트_이슈타입_리스트 = 지라프로젝트.getJiraIssueTypeEntities();
		JiraIssueTypeEntity 요구사항_이슈_타입 = 지라프로젝트_이슈타입_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_타입;
	}

	private JiraIssueStatusEntity 클라우드요구사항이슈상태검색(JiraProjectEntity 지라프로젝트) throws Exception {
		Set<JiraIssueStatusEntity> 지라프로젝트_이슈상태_리스트 = 지라프로젝트.getJiraIssueStatusEntities();
		JiraIssueStatusEntity 요구사항_이슈_상태 = 지라프로젝트_이슈상태_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_상태;
	}

	private JiraIssuePriorityEntity 요구사항이슈우선순위검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssuePriorityEntity> 지라서버_이슈우선순위_리스트 = 지라서버.getJiraIssuePriorityEntities();
		JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 지라서버_이슈우선순위_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_우선순위;
	}

	private JiraIssueResolutionEntity 요구사항이슈해결책검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssueResolutionEntity> 지라서버_이슈해결책_리스트 = 지라서버.getJiraIssueResolutionEntities();
		JiraIssueResolutionEntity 요구사항_이슈_해결책 = 지라서버_이슈해결책_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true"))
				.findFirst().orElse(null);
		return 요구사항_이슈_해결책;
	}

	private JiraProjectEntity 지라프로젝트검색(Long 지라_프로젝트_아이디) throws Exception {
		JiraProjectEntity 지라프로젝트_검색용_엔티티 = new JiraProjectEntity();
		지라프로젝트_검색용_엔티티.setC_id(지라_프로젝트_아이디);
		JiraProjectEntity 검색된_지라프로젝트 = jiraProject.getNode(지라프로젝트_검색용_엔티티);
		return 검색된_지라프로젝트;
	}

	private JiraServerEntity 지라서버검색(Long 지라서버_아이디) throws Exception {
		JiraServerEntity 지라서버_검색용_엔티티 = new JiraServerEntity();
		지라서버_검색용_엔티티.setC_id(지라서버_아이디);
		JiraServerEntity 검색된_지라서버 = jiraServer.getNode(지라서버_검색용_엔티티);
		return 검색된_지라서버;
	}
}
