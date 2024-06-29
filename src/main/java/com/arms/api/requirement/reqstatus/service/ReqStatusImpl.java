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
package com.arms.api.requirement.reqstatus.service;

import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.api.jira.jiraserver.model.enums.TextFormattingType;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqstatus.model.CRUDType;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.communicate.external.response.jira.*;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.DateUtils;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@AllArgsConstructor
@Service("reqStatus")
public class ReqStatusImpl extends TreeServiceImpl implements ReqStatus{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTreeMapService globalTreeMapService;

	@Autowired
	private ArmsDetailUrlConfig armsDetailUrlConfig;

	@Autowired
	protected Chat chat;

	@Autowired
	private EngineService EngineService;

	@Autowired
	private InternalService InternalService;

	@Autowired
	@Qualifier("jiraServer")
	private JiraServer jiraServer;

	@Autowired
	@Qualifier("jiraProject")
	private JiraProject jiraProject;

	@Autowired
	protected ModelMapper modelMapper;

	@Override
	public void 추가된_프로젝트_REQSTATUS_처리(ReqAddEntity reqAddEntity, Set<Long> 추가된_프로젝트_아이디_목록, PdServiceEntity 요구사항_제품서비스, List<ReqStatusEntity> reqStatusEntityList) throws Exception {
		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		// 추가되는 프로젝트 목록을 순회하며 REQSTATUS 데이터 생성처리
		for (Long ALM프로젝트_아이디 : 추가된_프로젝트_아이디_목록) {

			// REQSTATUS 데이터 세팅
			ReqStatusDTO reqStatusDTO = this.REQSTATUS_데이터_설정(ALM프로젝트_아이디, reqAddEntity, 요구사항_제품서비스, CRUDType.생성.getType());

			// 연결이 해제되었다가 다시 연결된 프로젝트에는 이미 생성했던 요구사항 이슈가 있음, 수정 시 삭제된 로직을 가져와서 그에 맞추어 검색
			Optional<ReqStatusEntity> 삭제된데이터검색 = Optional.ofNullable(reqStatusEntityList)
					.map(List::stream)
					.orElseGet(Stream::empty)
					.filter(reqStatusEntity -> reqStatusEntity.getC_req_link().equals(reqAddEntity.getC_id()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() != null)
					.filter(reqStatusEntity -> reqStatusEntity.getC_jira_project_link().equals(ALM프로젝트_아이디))
					.findFirst();

			// 삭제된 데이터가 있을 경우 해당 데이터 Soft Delete 제거, updateNode API 호출
			if (삭제된데이터검색.isPresent()) {
				chat.sendMessageByEngine("삭제된 데이터 발견되어 이슈 수정");
				reqStatusDTO.setC_id(삭제된데이터검색.get().getC_id());
				reqStatusDTO.setC_issue_delete_date(null);
				reqStatusDTO.setC_etc(CRUDType.수정.getType());

				ResponseEntity<?> 결과 = InternalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

				if (결과.getStatusCode().is2xxSuccessful()) {
					chat.sendMessageByEngine("기존에 Soft Delete 처리 된 지라 이슈를 복구하였습니다. 지라 이슈는 통계에 수집됩니다.");
				}
				else {
					logger.error("T_ARMS_REQSTATUS_" + 제품서비스_아이디 + " :: 수정 오류 :: " + reqStatusDTO.toString());
				}
			}
			else {
				// 없을 경우 REQSTATUS addNode API 호출
				ResponseEntity<?> 결과 = InternalService.요구사항_이슈_저장하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);

				if (!결과.getStatusCode().is2xxSuccessful()) {
					logger.error("T_ARMS_REQSTATUS_" + 제품서비스_아이디 + " :: 생성 오류 :: " + reqStatusDTO.toString());
				}
			}
		}
	}

	@Override
	public void 유지_또는_삭제된_프로젝트_REQSTATUS_처리(ReqAddEntity reqAddEntity, List<ReqStatusEntity> 지라프로젝트, PdServiceEntity 요구사항_제품서비스, String CRUD_타입) throws Exception {

		// 유지 또는 삭제되는 프로젝트 목록을 순회하며 REQSTATUS 데이터 생성처리
		for (ReqStatusEntity reqStatusEntity : 지라프로젝트) {

			Long ALM프로젝트_아이디 = reqStatusEntity.getC_jira_project_link();

			// REQSTATUS 수정 데이터 세팅
			ReqStatusDTO reqStatusDTO = this.REQSTATUS_데이터_설정(ALM프로젝트_아이디, reqAddEntity, 요구사항_제품서비스, CRUD_타입);

			// 업데이트할 c_id 설정
			reqStatusDTO.setC_id(reqStatusEntity.getC_id());
			// 요구사항 생성에서 에러난 상태의 요구사항의 경우 스케줄러가 다시 작동하기 전에
			// 수정을 하면 update로 바뀌게 되는데 이 때 issue key가 만들어지지 않고 수정 API를 호출하면 오류 발생하므로 해당 방어코드 추가
			if (!StringUtils.equals(CRUD_타입, CRUDType.생성.getType()) && reqStatusEntity.getC_issue_key() != null) {
				reqStatusDTO.setC_etc(CRUD_타입);
			}
			else {
				reqStatusDTO.setC_etc(CRUDType.생성.getType());
			}

			// REQSTATUS 데이터 updateNode API 호출
			ResponseEntity<?> 결과 = InternalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 요구사항_제품서비스.getC_id(), reqStatusDTO);

			if (!결과.getStatusCode().is2xxSuccessful()) {
				logger.error("T_ARMS_REQSTATUS_" + 요구사항_제품서비스.getC_id() + " :: 삭제 오류 :: " + reqStatusDTO.toString());
			}
		}
	}

	public void ALM서버_요구사항_생성_또는_수정_및_REQSTATUS_업데이트(ReqStatusEntity reqStatusEntity, Long 제품서비스_아이디) {

		// ALM 서버에 요구사항 생성 또는 수정할 REQSTATUS 데이터가 생성, 수정, 삭제인지 확인
		String CURD_타입;
		if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.생성.getType())) {
			CURD_타입 = "생성";
		}
		else if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.수정.getType())) {
			CURD_타입 = "수정";
		}
		else if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.소프트_삭제.getType())) {
			CURD_타입 = "Soft Delete 수정";
		}
		else if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.하드_삭제.getType())) {
			CURD_타입 = "ALM 요구사항 이슈 삭제";
		}
		else {
			CURD_타입 = "";
		}

		// REQSTATUS 데이터 기반 ALM 서버에 요구사항 이슈 생성 또는 수정
		ReqStatusEntity 생성결과 = this.ALM서버_요구사항_생성_또는_수정(reqStatusEntity);

		// REQSTATUS c_etc 컬럼이 완료(complete) 일 경우 생성완료 상태 그 외 실패
		if (생성결과.getC_etc() != null && StringUtils.equals(CRUDType.완료.getType(), 생성결과.getC_etc())) {
			chat.sendMessageByEngine("요구사항 이슈 :: "+ reqStatusEntity.getC_title() +" :: "
					+ CURD_타입 + ", ALM 서버를 확인해주세요.");
		}
		else {
			chat.sendMessageByEngine("요구사항 이슈 :: "+ reqStatusEntity.getC_title() +" :: "
					+ CURD_타입 + " 실패 :: " + 생성결과.getC_desc());
		}

		ReqStatusDTO updateReqStatusDTO = modelMapper.map(생성결과, ReqStatusDTO.class);

		// 생성 후 REQSTATUS 데이터 업데이트
		ResponseEntity<?> 업데이트_결과 = InternalService.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + 제품서비스_아이디, updateReqStatusDTO);

		// 업데이트 실패 시 메시지 전송
		if (!업데이트_결과.getStatusCode().is2xxSuccessful()) {
			logger.error("요구사항 생성 후 현황을 수정하던 중 오류가 발생하였습니다.");
			chat.sendMessageByEngine("요구사항 생성 후 현황을 수정하던 중 오류가 발생하였습니다.");
		}
	}

	private ReqStatusDTO REQSTATUS_데이터_설정(Long ALM프로젝트_아이디, ReqAddEntity savedReqAddEntity, PdServiceEntity 요구사항_제품서비스, String CRUD_타입) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();

		Long 요구사항_아이디 = savedReqAddEntity.getC_id();

		String 요구사항_제품서비스_버전목록_JSON = savedReqAddEntity.getC_req_pdservice_versionset_link();
		List<String> 요구사항_제품서비스_버전목록 = Arrays.asList(objectMapper.readValue(요구사항_제품서비스_버전목록_JSON, String[].class));

		Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

		Set<PdServiceVersionEntity> 제품서비스_버전_세트 = 요구사항_제품서비스.getPdServiceVersionEntities();
		Map<Long, String> 제품_버전아이디_버전명_맵 = 제품서비스_버전_세트.stream().collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));
		Map<Long,Set<Long>> 지라프로젝트_버전아이디_맵 = this.지라프로젝트_버전아이디_맵만들기(요구사항_제품서비스_버전목록);
		Set<Long> 버전아이디_세트 = 지라프로젝트_버전아이디_맵.get(ALM프로젝트_아이디);

		GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
		globalTreeMap.setJiraproject_link(ALM프로젝트_아이디);
		List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

		GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
				.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null)
				.findFirst()
				.orElse(null);

		Long ALM서버_아이디 = null;
		if (지라서버_글로벌트리맵 != null) {
			ALM서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();
		}

		ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
		//-- 추가된 프로젝트의 경우 설정
		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setRef(TreeConstant.First_Node_CID);
			reqStatusDTO.setC_type(TreeConstant.Leaf_Node_TYPE);
		}

		List<Long> 버전아이디_내림차순_목록 = null;
		String 버전아이디_내림차순_문자열 = null;
		String 버전명_내림차순_문자열 = null;
		String 버전ID목록 = null;

		if (버전아이디_세트 != null) {
			버전아이디_내림차순_목록 = 버전아이디_세트.stream()
					.sorted(Comparator.reverseOrder())
					.collect(Collectors.toList());

			버전아이디_내림차순_문자열 = 버전아이디_내림차순_목록.stream()
					.map(String::valueOf)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			버전명_내림차순_문자열 = 버전아이디_내림차순_목록.stream()
					.map(제품_버전아이디_버전명_맵::get)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			버전ID목록 = 버전아이디_내림차순_목록.stream()
					.map(String::valueOf)
					.collect(Collectors.joining(","));
		}

		//-- ARMS 요구사항의 매핑버전목록
		reqStatusDTO.setC_pds_version_name(버전명_내림차순_문자열);
		reqStatusDTO.setC_req_pdservice_versionset_link(버전아이디_내림차순_문자열);

		//-- 버전 연결 alm 프로젝트
		JiraProjectEntity 검색된_ALM프로젝트 = this.ALM프로젝트_검색(ALM프로젝트_아이디);
		Optional.ofNullable(검색된_ALM프로젝트).ifPresent(프로젝트 -> {
			Optional.ofNullable(프로젝트.getC_id()).ifPresent(reqStatusDTO::setC_jira_project_link);
			Optional.ofNullable(프로젝트.getC_jira_name()).ifPresent(reqStatusDTO::setC_jira_project_name);
			Optional.ofNullable(프로젝트.getC_jira_key()).ifPresent(reqStatusDTO::setC_jira_project_key);
			Optional.ofNullable(프로젝트.getC_jira_url()).ifPresent(reqStatusDTO::setC_jira_project_url);
		});

		//-- 프로젝트의 alm server
		JiraServerEntity 검색된_ALM서버 = this.ALM서버_검색(ALM서버_아이디);
		Optional.ofNullable(검색된_ALM서버).ifPresent(ALM서버 -> {
			Optional.ofNullable(ALM서버.getC_id()).ifPresent(reqStatusDTO::setC_jira_server_link);
			Optional.ofNullable(ALM서버.getC_jira_server_name()).ifPresent(reqStatusDTO::setC_jira_server_name);
			Optional.ofNullable(ALM서버.getC_jira_server_base_url()).ifPresent(reqStatusDTO::setC_jira_server_url);
		});

		String 요구사항_제목 = savedReqAddEntity.getC_title();
		if (StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType())) {
			요구사항_제목 = "[삭제된 요구사항 이슈] :: " + 요구사항_제목;
		}
		reqStatusDTO.setC_title(요구사항_제목);

		TextFormattingType 본문형식 = TextFormattingType.fromString(검색된_ALM서버.getC_server_contents_text_formatting_type());
		String 이슈본문 = 이슈_본문_설정(본문형식, CRUD_타입, savedReqAddEntity, 제품서비스_아이디, ALM서버_아이디, ALM프로젝트_아이디, 버전ID목록);
		reqStatusDTO.setC_contents(이슈본문);

		//-- 제품 서비스
		reqStatusDTO.setC_pdservice_link(제품서비스_아이디);
		reqStatusDTO.setC_pdservice_name(요구사항_제품서비스.getC_title());

		//-- ARMS REQADD 데이터를 설정
		reqStatusDTO.setC_req_link(요구사항_아이디);
		reqStatusDTO.setC_req_name(savedReqAddEntity.getC_title());

		//-- ARMS 요구사항 오너를 제품 서비스으 오너로 설정
		String 요구사항_오너 = "admin";
		if (요구사항_제품서비스.getC_pdservice_owner() != null) {
			요구사항_오너 = 요구사항_제품서비스.getC_pdservice_owner();
		}
		reqStatusDTO.setC_req_owner(요구사항_오너);

		//-- ARMS 요구사항 요청자를 기본적으로 요구사항 REQSTATUS 보고자로 설정
		Optional.ofNullable(savedReqAddEntity.getC_req_writer()).ifPresent(reqStatusDTO::setC_issue_reporter);

		//-- ARMS 요구사항 시간 데이터
		Optional.ofNullable(savedReqAddEntity.getC_req_start_date()).ifPresent(reqStatusDTO::setC_req_start_date);
		Optional.ofNullable(savedReqAddEntity.getC_req_end_date()).ifPresent(reqStatusDTO::setC_req_end_date);

		//-- ARMS 요구사항 우선순위
		Optional.ofNullable(savedReqAddEntity.getReqPriorityEntity()).ifPresent(reqPriority -> {
			Optional.ofNullable(reqPriority.getC_id()).ifPresent(reqStatusDTO::setC_req_priority_link);
			Optional.ofNullable(reqPriority.getC_title()).ifPresent(reqStatusDTO::setC_req_priority_name);
		});
		//-- ARMS 요구사항 상태
		Optional.ofNullable(savedReqAddEntity.getReqStateEntity()).ifPresent(reqState -> {
			Optional.ofNullable(reqState.getC_id()).ifPresent(reqStatusDTO::setC_req_state_link);
			Optional.ofNullable(reqState.getC_title()).ifPresent(reqStatusDTO::setC_req_state_name);
		});
		//-- ARMS 요구사항 난이도
		Optional.ofNullable(savedReqAddEntity.getReqDifficultyEntity()).ifPresent(reqDifficulty -> {
			Optional.ofNullable(reqDifficulty.getC_id()).ifPresent(reqStatusDTO::setC_req_difficulty_link);
			Optional.ofNullable(reqDifficulty.getC_title()).ifPresent(reqStatusDTO::setC_req_difficulty_name);
		});

		//-- ARMS 요구사항 작업량 데이터
		Optional.ofNullable(savedReqAddEntity.getC_req_total_resource()).ifPresent(reqStatusDTO::setC_req_total_resource);
		Optional.ofNullable(savedReqAddEntity.getC_req_plan_resource()).ifPresent(reqStatusDTO::setC_req_plan_resource);
		Optional.ofNullable(savedReqAddEntity.getC_req_total_time()).ifPresent(reqStatusDTO::setC_req_total_time);
		Optional.ofNullable(savedReqAddEntity.getC_req_plan_time()).ifPresent(reqStatusDTO::setC_req_plan_time);

		Date date = new Date();
		reqStatusDTO.setC_issue_update_date(date);

		// 요구사항 ALM 서버 설정 상태로 c_etc 컬럼을 crud type별 처리
		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setC_issue_create_date(date);
		}
		else if (StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType())) {
			reqStatusDTO.setC_issue_delete_date(date);
		}
		else if (StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType())) {
			reqStatusDTO.setC_issue_delete_date(date);
		}

		if (StringUtils.equals(CRUD_타입, CRUDType.생성.getType())) {
			reqStatusDTO.setC_etc(CRUD_타입);
		}

		logger.info("ReqStatusImpl = reqStatusDTO :: " + objectMapper.writeValueAsString(reqStatusDTO));

		return reqStatusDTO;
	}

	private Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵만들기(List<String> 디비에저장된_제품서비스_하위의_버전리스트) {
		Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵 = new HashMap<>();
		for (String 디비에저장된_제품서비스하위_버전 : 디비에저장된_제품서비스_하위의_버전리스트) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
			List<GlobalTreeMapEntity> 버전_지라프로젝트_목록 = globalTreeMapService.findAllBy(globalTreeMap).stream()
					.filter(엔티티 -> 엔티티.getJiraproject_link() != null).collect(Collectors.toList());

			for (GlobalTreeMapEntity 엔티티 : 버전_지라프로젝트_목록) {
				Long ALM프로젝트_아이디 = 엔티티.getJiraproject_link();
				Long 버전_아이디 = 엔티티.getPdserviceversion_link();

				if(지라프로젝트_버전아이디_맵.containsKey(ALM프로젝트_아이디)) {
					지라프로젝트_버전아이디_맵.get(ALM프로젝트_아이디).add(버전_아이디);
				} else {
					Set<Long> 버전_셋 = new HashSet<>();
					버전_셋.add(버전_아이디);
					지라프로젝트_버전아이디_맵.put(ALM프로젝트_아이디, 버전_셋);
				}
			}
		}

		return 지라프로젝트_버전아이디_맵;
	}

	public ReqStatusEntity ALM서버_요구사항_생성_또는_수정(ReqStatusEntity reqStatusEntity) {

		JiraServerEntity 검색된_지라서버= this.ALM서버_검색(reqStatusEntity.getC_jira_server_link());
		if (검색된_지라서버 == null) {
			String 실패_이유 = "ALM서버_요구사항_생성 작동 중 ALM 서버 조회 오류";

			logger.error(실패_이유);
			chat.sendMessageByEngine(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);

			return reqStatusEntity;
		}

		JiraProjectEntity 검색된_지라프로젝트 = this.ALM프로젝트_검색(reqStatusEntity.getC_jira_project_link());
		if (검색된_지라프로젝트 == null) {
			String 실패_이유 = "ALM서버_요구사항_생성 작동 중 ALM 서버 프로젝트 조회 오류";

			logger.error(실패_이유);
			chat.sendMessageByEngine(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);

			return reqStatusEntity;
		}

		if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
			String 실패_이유 = 검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
					+ 검색된_지라프로젝트.getC_jira_name() + "는 소프트 딜리트 처리된 상태입니다. 연결된 프로젝트 정보 확인이 필요합니다.";

			logger.info(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);
			return reqStatusEntity;
		}

		ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());

		// 이슈 유형
		JiraIssueTypeEntity 요구사항_이슈_타입 = null;
		if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
			요구사항_이슈_타입= 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
		}
		else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
			요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
		}

		if (요구사항_이슈_타입 == null) {
			String 실패_이유 = 검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
					+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_타입이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.";

			logger.error(실패_이유);
			chat.sendMessageByEngine(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);

			return reqStatusEntity;
		}

		지라이슈필드_데이터.프로젝트 프로젝트 = 지라이슈필드_데이터.프로젝트.builder().id(String.valueOf(검색된_지라프로젝트.getC_desc()))
				.key(검색된_지라프로젝트.getC_jira_key())
				.build();

		지라이슈유형_데이터 유형 = new 지라이슈유형_데이터();
		유형.setId(요구사항_이슈_타입.getC_issue_type_id());

		String 요구사항_제목 = reqStatusEntity.getC_title();
		String 요구사항_내용 = reqStatusEntity.getC_contents();
		Date 시작일 = reqStatusEntity.getC_req_start_date();
		Date 종료일 = reqStatusEntity.getC_req_end_date();

		지라이슈필드_데이터.지라이슈필드_데이터Builder 요구사항이슈_필드빌더 = 지라이슈필드_데이터
				.builder()
				.project(프로젝트)
				.issuetype(유형)
				.summary(요구사항_제목)
				.description(요구사항_내용)
				.startDate(시작일)
				.dueDate(종료일);

		// REQSTATUS c_etc 컬럼이 delete 일 경우 삭제 처리 대신 라벨  삭제 처리(ALM 지라 서버의 경우)
		if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.소프트_삭제.getType())) {
			String 삭제라벨 = "삭제된_요구사항_이슈";
			요구사항이슈_필드빌더.labels(List.of(삭제라벨));
		}

		JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);
		지라이슈우선순위_데이터 우선순위;
		if (요구사항_이슈_우선순위 != null) {
			reqStatusEntity.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
			reqStatusEntity.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());

			우선순위 = new 지라이슈우선순위_데이터();
			우선순위.setId(요구사항_이슈_우선순위.getC_issue_priority_id());

			요구사항이슈_필드빌더.priority(우선순위);
		}
		else if (reqStatusEntity.getC_issue_priority_link() == null && serverType.equals(ServerType.REDMINE_ON_PREMISE)){
			String 실패_이유 = 검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
					+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_우선순위가 없습니다. 이슈 우선순위 기본 설정 확인이 필요합니다.";

			logger.error(실패_이유);
			chat.sendMessageByEngine(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);

			return reqStatusEntity;
		}
		else {
			logger.info("요구사항_이슈_우선순위 기본값이 없습니다. 요구사항은 등록됩니다.");
		}

		지라이슈필드_데이터 요구사항이슈_필드 = 요구사항이슈_필드빌더.build();
		지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
				.builder()
				.fields(요구사항이슈_필드)
				.build();

		logger.info("[ ReqAddImpl :: ALM서버_요구사항_생성 ] ::engine parameter -> " + 요구사항_이슈.toString());

		지라이슈_데이터 생성된_요구사항_이슈 = null;
		try {
			if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.생성.getType())) {
				생성된_요구사항_이슈 = EngineService.이슈_생성하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 요구사항_이슈);
			}
			else if (StringUtils.equals(reqStatusEntity.getC_etc(), CRUDType.하드_삭제.getType())) {
				Map<String, Object> 삭제결과 = EngineService.이슈_삭제하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), reqStatusEntity.getC_issue_key());

				if (!((boolean) 삭제결과.get("success"))) {
					String 실패_이유 = String.format("%s 서버 :: %s 프로젝트 :: 요구사항 %s 중 실패하였습니다. :: %s",
							검색된_지라서버.getC_jira_server_base_url(),
							검색된_지라프로젝트.getC_jira_name(),
							reqStatusEntity.getC_etc(),
							삭제결과.get("message")
					);

					logger.error(실패_이유);
					chat.sendMessageByEngine(실패_이유);
					reqStatusEntity.setC_desc(실패_이유);

					return reqStatusEntity;
				}
			}
			else {
				Map<String, Object> 수정결과 = EngineService.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), reqStatusEntity.getC_issue_key(), 요구사항_이슈);

				if (!((boolean) 수정결과.get("success"))) {
					String 실패_이유 = String.format("%s 서버 :: %s 프로젝트 :: 요구사항 %s 중 실패하였습니다. :: %s",
							검색된_지라서버.getC_jira_server_base_url(),
							검색된_지라프로젝트.getC_jira_name(),
							reqStatusEntity.getC_etc(),
							수정결과.get("message")
					);

					logger.error(실패_이유);
					chat.sendMessageByEngine(실패_이유);
					reqStatusEntity.setC_desc(실패_이유);

					return reqStatusEntity;
				}
			}
		}
		catch (Exception e) {
			String 실패_이유 = 검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
					+ 검색된_지라프로젝트.getC_jira_name() + "에 요구사항 " + reqStatusEntity.getC_etc() + " 중 실패하였습니다. :: " + e.getMessage();

			logger.error(실패_이유);
			chat.sendMessageByEngine(실패_이유);
			reqStatusEntity.setC_desc(실패_이유);

			return reqStatusEntity;
		}

		if (StringUtils.equals(CRUDType.수정.getType(), reqStatusEntity.getC_etc())) {
			this.ALM_이슈상태_업데이트(reqStatusEntity);
		}

		reqStatusEntity.setC_etc(CRUDType.완료.getType());
		this.REQSTATUS_ALM_데이터동기화(생성된_요구사항_이슈, reqStatusEntity);

		return reqStatusEntity;
	}

	private void REQSTATUS_ALM_데이터동기화(지라이슈_데이터 생성된_요구사항_이슈, ReqStatusEntity reqStatusEntity) {

		if (생성된_요구사항_이슈 == null) {
			return;
		}
		logger.info("[ ReqStatusImpl :: ALM서버_요구사항_생성 ] :: 생성된_요구사항 이슈 -> {}", 생성된_요구사항_이슈.toString());
		reqStatusEntity.setC_issue_key(생성된_요구사항_이슈.getKey());
		reqStatusEntity.setC_issue_url(생성된_요구사항_이슈.getSelf());

		지라이슈필드_데이터 필드_데이터 = 생성된_요구사항_이슈.getFields();
		if (필드_데이터 == null) {
			return;
		}

		if (필드_데이터.getAssignee() != null && 필드_데이터.getAssignee().getDisplayName() != null) {
			reqStatusEntity.setC_issue_assignee(필드_데이터.getAssignee().getDisplayName());
		}

		if (필드_데이터.getPriority() != null && 필드_데이터.getPriority().getName() != null) {
			reqStatusEntity.setC_issue_priority_name(필드_데이터.getPriority().getName());
		}

		if (필드_데이터.getReporter() != null && 필드_데이터.getReporter().getDisplayName() != null) {
			reqStatusEntity.setC_issue_reporter(필드_데이터.getReporter().getDisplayName());
		}

		if (필드_데이터.getResolution() != null && 필드_데이터.getResolution().getName() != null) {
			reqStatusEntity.setC_issue_resolution_name(필드_데이터.getResolution().getName());
		}

		if (필드_데이터.getStatus() != null && 필드_데이터.getStatus().getName() != null) {
			reqStatusEntity.setC_issue_status_name(필드_데이터.getStatus().getName());
		}
	}

	public JiraIssueTypeEntity 요구사항_이슈타입검색(Set<JiraIssueTypeEntity> issueTypes) {

		// getC_check가 "true"인 엔티티를 우선적으로 검색
		Optional<JiraIssueTypeEntity> checkTrueEntity = issueTypes.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true") &&
						(entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst();

		if (checkTrueEntity.isPresent()) {
			return checkTrueEntity.get();
		}

		// getC_check가 "true"인 엔티티가 없을 경우, "arms-requirement"를 가진 엔티티를 검색
		return issueTypes.stream()
				.filter(entity -> StringUtils.equals(entity.getC_issue_type_name(), "arms-requirement") &&
						(entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst()
				.orElse(null);
	}

	private JiraIssuePriorityEntity 요구사항_이슈우선순위검색(JiraServerEntity 지라서버) {
		Set<JiraIssuePriorityEntity> 지라서버_이슈우선순위_리스트 = 지라서버.getJiraIssuePriorityEntities();
		JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 지라서버_이슈우선순위_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true")
									&& (entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst().orElse(null);
		return 요구사항_이슈_우선순위;
	}

	public JiraIssueStatusEntity 매핑된_요구사항_이슈상태_검색(Set<JiraIssueStatusEntity> issueStatusEntities, Long 변경할_ARMS_상태아이디) {
		if (issueStatusEntities == null || 변경할_ARMS_상태아이디 == null) {
			return null;
		}

		// ARMS 요구사항의 상태와 연결된 ALM 이슈 상태 중 findFirst 로 첫번째 조회되 상태로 update c_req_state_mapping_link 변경 필요
		return issueStatusEntities.stream()
				.filter(entity -> entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete"))
				.filter(entity -> entity.getC_req_state_mapping_link() != null
						&& entity.getC_req_state_mapping_link().equals(변경할_ARMS_상태아이디))

				.findFirst()
				.orElse(null);
	}

	public JiraProjectEntity ALM프로젝트_검색(Long ALM_프로젝트_아이디) {
		if (ALM_프로젝트_아이디 == null) {
			return null;
		}

		JiraProjectEntity jiraProjectEntity = null;
		try {
			jiraProjectEntity = TreeServiceUtils.getNode(jiraProject, ALM_프로젝트_아이디, JiraProjectEntity.class);
			boolean isSoftDelete = Optional.ofNullable(jiraProjectEntity)
					.map(JiraProjectEntity::getC_etc)
					.map("delete"::equals)
					.orElse(false);

			if (isSoftDelete) {
				jiraProjectEntity = null;
			}
		}
		catch (Exception e) {
			logger.error("ALM프로젝트_검색 :: 프로젝트 아이디 :: " + ALM_프로젝트_아이디 + " :: " + e.getMessage());
		}

		return jiraProjectEntity;
	}

	public JiraServerEntity ALM서버_검색(Long ALM서버_아이디) {
		if (ALM서버_아이디 == null) {
			return null;
		}

		JiraServerEntity jiraServerEntity = null;
		try {
			jiraServerEntity = TreeServiceUtils.getNode(jiraServer, ALM서버_아이디, JiraServerEntity.class);
		}
		catch (Exception e) {
			logger.error("ALM서버_검색 :: 서버 아이디 :: " + ALM서버_아이디 + " :: " + e.getMessage());
		}

		return jiraServerEntity;
	}

	private String 등록_및_수정_ARMS_안내문가져오기(ReqAddEntity reqAddEntity, Long 제품서비스_아이디,
									 Long ALM서버_아이디, Long ALM_프로젝트_아이디, String 버전아이디목록) {

		String 추가된_요구사항의_아이디 = reqAddEntity.getC_id().toString();

		String 버전아이디목록_파싱 = 버전아이디목록.replaceAll("\\[|\\]|\"", "").replaceAll(",", ",");

		String 시작일 = Optional.ofNullable(reqAddEntity.getC_req_start_date())
						.map(date -> DateUtils.format("yyyy-MM-dd", date))
						.orElse("시작일 데이터를 확인할 수 없습니다. 버전의 시작일 확인이 필요합니다");
		String 종료일 = Optional.ofNullable(reqAddEntity.getC_req_end_date())
						.map(date -> DateUtils.format("yyyy-MM-dd", date))
						.orElse("종료일 데이터를 확인할 수 없습니다. 버전의 종료일 확인이 필요합니다");

		String ALM서버_아이디_글자 = Optional.ofNullable(ALM서버_아이디).map(Object::toString).orElse("");
		String ALM_프로젝트_아이디_글자 = Optional.ofNullable(ALM_프로젝트_아이디).map(Object::toString).orElse("");

		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 자동으로 관리되므로,\n" +
				"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
				"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
				"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
				"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
				"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
				"자세한 요구사항 내용 확인 ⇒ http://" + armsDetailUrlConfig.getAddress() + "/arms/detail.html?page=detail&pdService=" + 제품서비스_아이디 +
				"&pdServiceVersion=" + 버전아이디목록_파싱 +
				"&reqAdd=" + 추가된_요구사항의_아이디 + "&jiraServer=" + ALM서버_아이디_글자 + "&jiraProject=" + ALM_프로젝트_아이디_글자 + "\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n\n" +
				"시작 일자 : "+ 시작일 +"\n" +
				"종료 일자 : "+ 종료일 +"\n\n" +
				"※ 『 아래는 입력된 요구사항 내용입니다. 』\n\n\n";

		return 이슈내용;
	}

	private String 삭제_이슈_ARMS_안내문가져오기() {
		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 삭제 된 이슈입니다.,\n" +
				"✔ 삭제 된 이슈는 통계에 수집되지 않습니다. \n\n\n";

		return 이슈내용;
	}

	private String 이슈_본문_설정(TextFormattingType 본문형식, String CRUD_타입, ReqAddEntity reqAddEntity,
							Long 제품서비스_아이디, Long ALM서버_아이디, Long ALM_프로젝트_아이디, String 버전아이디목록) {

		String 이슈_본문_전체;
		if (StringUtils.equals(CRUD_타입, CRUDType.소프트_삭제.getType()) || StringUtils.equals(CRUD_타입, CRUDType.하드_삭제.getType())) {
			이슈_본문_전체 = this.삭제_이슈_ARMS_안내문가져오기();
		}
		else {
			이슈_본문_전체 = 등록_및_수정_ARMS_안내문가져오기(reqAddEntity, 제품서비스_아이디, ALM서버_아이디, ALM_프로젝트_아이디, 버전아이디목록);
		}

		String ARMS_요구사항_설명 = Optional.ofNullable(reqAddEntity.getC_req_contents()).orElse("이슈 본문 내용 무");

		if (본문형식 == TextFormattingType.MARKDOWN || 본문형식 == TextFormattingType.HTML) {
			이슈_본문_전체 = 이슈_본문_전체.replaceAll("\n", "<br>") + ARMS_요구사항_설명;
		}
		else {
			이슈_본문_전체 = 이슈_본문_전체 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean(ARMS_요구사항_설명, Whitelist.basic())), "&nbsp;", " ");
		}

		return 이슈_본문_전체;
	}

	public void ALM_이슈상태_업데이트(ReqStatusEntity reqStatusEntity) {

		if (reqStatusEntity == null || reqStatusEntity.getC_issue_key() == null || reqStatusEntity.getC_issue_delete_date() != null) {
			return;
		}

		Long 변경할_ARMS_상태아이디 = reqStatusEntity.getC_req_state_link();
		String 이슈_키_또는_아이디 = reqStatusEntity.getC_issue_key();

		JiraProjectEntity 검색된_ALM프로젝트 = this.ALM프로젝트_검색(reqStatusEntity.getC_jira_project_link());
		JiraServerEntity 검색된_ALM서버 = this.ALM서버_검색(reqStatusEntity.getC_jira_server_link());

		if (검색된_ALM서버 == null || 검색된_ALM프로젝트 == null) {
			return;
		}

		ServerType 서버_유형 = ServerType.fromString(검색된_ALM서버.getC_jira_server_type());

		JiraIssueStatusEntity 요구사항_이슈_상태 = null;

		if (서버_유형.equals(ServerType.JIRA_CLOUD) ) {
			요구사항_이슈_상태 = this.매핑된_요구사항_이슈상태_검색(검색된_ALM프로젝트.getJiraIssueStatusEntities(), 변경할_ARMS_상태아이디);
		}
		else if (서버_유형.equals(ServerType.JIRA_ON_PREMISE)|| 서버_유형.equals(ServerType.REDMINE_ON_PREMISE)) {
			요구사항_이슈_상태 = this.매핑된_요구사항_이슈상태_검색(검색된_ALM서버.getJiraIssueStatusEntities(), 변경할_ARMS_상태아이디);
		}

		if (요구사항_이슈_상태 != null) { // 메핑된 상태 값이 없으면 ALM까지 데이터 전파 필요 없음
			String 변경할_이슈상태_아이디 = 요구사항_이슈_상태.getC_issue_status_id();
			Map<String, Object> 변경_결과 = EngineService.이슈_상태_변경하기(Long.parseLong(검색된_ALM서버.getC_jira_server_etc()),
																	이슈_키_또는_아이디,
																	변경할_이슈상태_아이디);

			if (!((boolean) 변경_결과.get("success"))) {
				String 실패_이유 = String.format("%s 서버 :: %s 프로젝트 :: 요구사항 수정 중 실패하였습니다. :: 해당 업무 흐름으로 변경이 불가능 합니다. :: %s",
												검색된_ALM서버.getC_jira_server_base_url(),
												검색된_ALM프로젝트.getC_jira_name(),
												변경_결과.get("message"));

				logger.error(실패_이유);
				chat.sendMessageByEngine(실패_이유);
			}
			else {
				reqStatusEntity.setC_issue_status_link(Long.valueOf(요구사항_이슈_상태.getC_issue_status_id()));
				reqStatusEntity.setC_issue_status_name(요구사항_이슈_상태.getC_issue_status_name());

				String 성공_메세지 = String.format("%s 서버 :: %s 프로젝트 :: %s 요구사항 상태를 변경하였습니다. :: %s",
													검색된_ALM서버.getC_jira_server_base_url(),
													검색된_ALM프로젝트.getC_jira_name(),
													reqStatusEntity.getC_title(),
													요구사항_이슈_상태.getC_issue_status_name());

				chat.sendMessageByEngine(성공_메세지);
			}
		}
	}
}