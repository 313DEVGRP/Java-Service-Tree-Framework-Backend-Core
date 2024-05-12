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

import com.arms.api.analysis.common.AggregationRequestDTO;
import com.arms.api.analysis.common.IsReqType;
import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraissuepriority.model.JiraIssuePriorityEntity;
import com.arms.api.jira.jiraissueresolution.model.JiraIssueResolutionEntity;
import com.arms.api.jira.jiraissuestatus.model.JiraIssueStatusEntity;
import com.arms.api.jira.jiraissuetype.model.JiraIssueTypeEntity;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.model.enums.ServerType;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.*;
import com.arms.api.requirement.reqadd.model.요구사항별_담당자_목록.요구사항_담당자;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.response.jira.*;
import com.arms.api.util.communicate.external.엔진통신기;
import com.arms.api.util.communicate.internal.내부통신기;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.DateUtils;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.modelmapper.ModelMapper;
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
	private 엔진통신기 엔진통신기;

	@Autowired
	private 내부통신기 내부통신기;

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

	@Autowired
	@Qualifier("reqStatus")
	private ReqStatus reqStatus;

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

		Long 제품서비스_아이디 = 추가된_요구사항의_제품서비스.getC_id();
		Set<PdServiceVersionEntity> 제품서비스_버전_세트 = 추가된_요구사항의_제품서비스.getPdServiceVersionEntities();
		Map<Long, String> 제품_버전아이디_버전명_맵 = 제품서비스_버전_세트.stream().collect(Collectors.toMap(PdServiceVersionEntity::getC_id, PdServiceVersionEntity::getC_title));

		Map<Long,Set<Long>> 지라프로젝트_버전아이디_맵 = 지라프로젝트_버전아이디_맵만들기(디비에저장된_제품서비스_하위의_버전리스트);

		List<Long> 지라프로젝트_아이디_목록 = 지라프로젝트_버전아이디_맵.keySet().stream().collect(Collectors.toList());
		int 소프트딜리트_프로젝트수 = 0;

		/***
		 * 요구사항 생성 전 Soft Delete된 프로젝트 확인 및 선택된 이슈 유형, 우선순위 검증 로직
		 * 반목문 돌며 이슈 생성 중 오류가 발생 시 암스에는 없지만 ALM 서버에 요구사항 이슈는 생성되는 현상 방지
		 ***/
		for (Long 지라프로젝트_아이디 : 지라프로젝트_아이디_목록) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setJiraproject_link(지라프로젝트_아이디);
			List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

			GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
					.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null)
					.findFirst()
					.orElse(null);

			Long 지라서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();
			JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);

			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라프로젝트_아이디);
			// 이슈를 생성하려는 버전의 프로젝트 목록이 모두 Soft Delete 처리된 프로젝트 경우 오류 처리
			if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
				logger.info(검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
						+ 검색된_지라프로젝트.getC_jira_name() + "에 소프트 딜리트 처리된 상태입니다.");
				소프트딜리트_프로젝트수++;
				if (소프트딜리트_프로젝트수 == 지라프로젝트_아이디_목록.size()) {
					throw new Exception("삭제된 프로젝트에 이슈를 생성을 시도했습니다. 연결된 프로젝트 정보 확인이 필요합니다.");
				}
				continue;
			}

			// 이슈 유형 기본설정, arms-requirement 확인
			ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());
			JiraIssueTypeEntity 요구사항_이슈_타입 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_타입= 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
			}
			else {
				logger.info("지라 서버 타입에 알 수 없는 값이 들어있습니다. :: " + 검색된_지라서버.getC_jira_server_type());
				throw new Exception("unknown jira server type :: " + 검색된_지라서버.getC_jira_server_type());
			}

			if (요구사항_이슈_타입 == null) {
				logger.error(검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
						+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_타입이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
				chat.sendMessageByEngine(검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
						+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_타입이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
				throw new Exception(검색된_지라서버.getC_jira_server_base_url() + " :: " + 검색된_지라프로젝트.getC_title() +
						" 프로젝트에 선택된 이슈유형이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
			}

			// 이슈 우선순위 기본설정 확인
			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);
			if (serverType.equals(ServerType.REDMINE_ON_PREMISE) && 요구사항_이슈_우선순위 == null) {
				logger.error(검색된_지라서버.getC_jira_server_base_url() +
						" 서버에 선택된 요구사항_이슈_우선순위가 없습니다. 이슈 우선순위 기본 설정 확인이 필요합니다.");
				chat.sendMessageByEngine(검색된_지라서버.getC_jira_server_base_url() +
						" 서버에 선택된 요구사항_이슈_우선순위가 없습니다. 이슈 우선순위 기본 설정 확인이 필요합니다.");
				throw new Exception(검색된_지라서버.getC_jira_server_base_url() +
						" 서버에 선택된 요구사항_이슈_우선순위가 없습니다. 이슈 우선순위 기본 설정 확인이 필요합니다.");
			}

			/***
			 * 갱신하지 않은 상황에서 ALM 서버에서만 데이터가 삭제되었는데 이슈를 생성하는 경우
			 * 이슈 생성 전 해당 버전과 연결된 프로젝트와 이슈 유형을 ALM 서버에 API 호출하여 ALM 서버에서 직접 확인 후 선 오류 처리하는 로직을 추가하는 것 고민
			 * 검증 여부에 따라 요구사항 이슈 생성 or 오류 처리
			 ***/
		}

		// 각 지라프로젝트를 활용해서 요구사항 만들기.
		for (Long 지라프로젝트_아이디 : 지라프로젝트_아이디_목록) {
			GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
			globalTreeMap.setJiraproject_link(지라프로젝트_아이디);
			List<GlobalTreeMapEntity> 지라프로젝트에_연결된정보들 = globalTreeMapService.findAllBy(globalTreeMap);

			GlobalTreeMapEntity 지라서버_글로벌트리맵 = 지라프로젝트에_연결된정보들.stream()
					.filter(글로벌트리맵 -> 글로벌트리맵.getJiraserver_link() != null) // 특정값이 null이 아닌 엔티티들로 필터링
					.findFirst() // 첫 번째로 찾은 엔티티를 반환 (단일 값)
					.orElse(null); // 만약 찾은 엔티티가 없으면 null 반환

			Long 지라서버_아이디 = 지라서버_글로벌트리맵.getJiraserver_link();

			logger.info("지라 서버 링크 = " + 지라서버_아이디);

			JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);

			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라프로젝트_아이디);
			// Soft Delete된 프로젝트의 경우 이슈생성을 하지 않고 넘어가도록 추가
			if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
				continue;
			}

			ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());

			JiraIssueTypeEntity 요구사항_이슈_타입 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_타입= 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
			}

			if (요구사항_이슈_타입 == null) {
				logger.error(검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
						+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_타입이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
				throw new Exception(검색된_지라서버.getC_jira_server_base_url() + " :: " + 검색된_지라프로젝트.getC_title() +
						" 프로젝트에 선택된 이슈유형이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
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
			logger.info("요구사항_이슈_타입 = " + 요구사항_이슈_타입.getC_issue_type_name());
			logger.info("요구사항_이슈_내용 요구사항아이디 링크 URL = " + 추가된_요구사항의_아이디);

			지라이슈필드_데이터.프로젝트 프로젝트 = 지라이슈필드_데이터.프로젝트.builder().id(검색된_지라프로젝트.getC_desc())
					.key(검색된_지라프로젝트.getC_jira_key())
					.build();

			지라이슈유형_데이터 유형 = new 지라이슈유형_데이터();
			유형.setId(요구사항_이슈_타입.getC_issue_type_id());

			지라이슈필드_데이터.보고자 암스서버보고자 = new 지라이슈필드_데이터.보고자();
			암스서버보고자.setName(검색된_지라서버.getC_jira_server_connect_id());

			지라이슈필드_데이터.담당자 암스서버담당자 = new 지라이슈필드_데이터.담당자();
			암스서버담당자.setName(검색된_지라서버.getC_jira_server_connect_id());

			String 버전ID목록 = 버전아이디_내림차순_목록.stream().map(String::valueOf).collect(Collectors.joining(","));

			String 시작일 = savedReqAddEntity.getC_req_start_date() == null
					? "시작일 데이터를 확인할 수 없습니다. 버전의 시작일 확인이 필요합니다"
					: DateUtils.format("yyyy-MM-dd", savedReqAddEntity.getC_req_start_date());
			String 종료일 = savedReqAddEntity.getC_req_end_date() == null
					? "종료일 데이터를 확인할 수 없습니다. 버전의 종료일 확인이 필요합니다"
					: DateUtils.format("yyyy-MM-dd", savedReqAddEntity.getC_req_end_date());

			String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
					"✔ 본 이슈는 자동으로 관리되므로,\n" +
					"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
					"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
					"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
					"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
					"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
					"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
					"자세한 요구사항 내용 확인 ⇒ http://" + armsDetailUrlConfig.getAddress() + "/arms/detail.html?page=detail&pdService=" + 제품서비스_아이디 +
					"&reqAdd=" + 추가된_요구사항의_아이디 + "&jiraServer=" + 지라서버_아이디 + "&jiraProject=" + 지라프로젝트_아이디 +
					"&pdServiceVersion=" + 버전ID목록 + "\n" +
					"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n\n" +
					"시작 일자 : "+ 시작일 +"\n" +
					"종료 일자 : "+ 종료일 +"\n\n" +
					"※ 『 아래는 입력된 요구사항 내용입니다. 』\n\n\n";

			이슈내용 = 이슈내용 + StringUtils.replaceText(StringUtils.removeHtmlTags(Jsoup.clean(savedReqAddEntity.getC_req_contents(), Whitelist.none())),"&nbsp;", " ");

			지라이슈필드_데이터.지라이슈필드_데이터Builder 요구사항이슈_필드빌더 = 지라이슈필드_데이터
					.builder()
					.project(프로젝트)
					.issuetype(유형)
					.summary(savedReqAddEntity.getC_title())
					.description(이슈내용)
					.startDate(savedReqAddEntity.getC_req_start_date())
					.dueDate(savedReqAddEntity.getC_req_end_date());

			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);
			지라이슈우선순위_데이터 우선순위;
			if (요구사항_이슈_우선순위 != null) {
				logger.info("요구사항_이슈_우선순위 = " + 요구사항_이슈_우선순위.getC_issue_priority_name());
				우선순위 = new 지라이슈우선순위_데이터();
				우선순위.setId(요구사항_이슈_우선순위.getC_issue_priority_id());

				요구사항이슈_필드빌더.priority(우선순위);
			}
			else {
				logger.info("요구사항_이슈_우선순위 기본값이 없습니다. 요구사항은 등록됩니다.");
			}

			지라이슈필드_데이터 요구사항이슈_필드 = 요구사항이슈_필드빌더.build();
			지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
					.builder()
					.fields(요구사항이슈_필드)
					.build();

			logger.info("[ ReqAddImpl :: addReqNodeNew ] ::engine parameter -> " + objectMapper.writeValueAsString(요구사항_이슈));

			지라이슈_데이터 생성된_요구사항_이슈 = 엔진통신기.이슈_생성하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 요구사항_이슈);

			logger.info("[ ReqAddImpl :: addReqNodeNew ] :: 생성된_요구사항 이슈 -> {}", 생성된_요구사항_이슈.toString());
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

			logger.info("[ ReqAddImpl :: addReqNodeNew ] :: 요구사항의 우선순위,상태,난이도 -> {} {} {}",
					savedReqAddEntity.getReqPriorityEntity().getC_title(),
					savedReqAddEntity.getReqStateEntity().getC_title(),
					savedReqAddEntity.getReqDifficultyEntity().getC_title());

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
				reqStatusDTO.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
				reqStatusDTO.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
			}

			reqStatusDTO.setC_req_owner("admin");
			reqStatusDTO.setC_issue_reporter(암스서버보고자.getName());
			reqStatusDTO.setC_issue_assignee(암스서버담당자.getName());

			// 요구사항 생성 시 reqStatus start date도 함께 추가
			reqStatusDTO.setC_issue_create_date(savedReqAddEntity.getC_req_create_date());
			reqStatusDTO.setC_req_start_date(savedReqAddEntity.getC_req_start_date());
			reqStatusDTO.setC_req_end_date(savedReqAddEntity.getC_req_end_date());

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

	private Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵만들기(List<String> 디비에저장된_제품서비스_하위의_버전리스트) {
		Map<Long, Set<Long>> 지라프로젝트_버전아이디_맵 = new HashMap<>();
		for (String 디비에저장된_제품서비스하위_버전 : 디비에저장된_제품서비스_하위의_버전리스트) {
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

		return 지라프로젝트_버전아이디_맵;
	}

	@Override
	@Transactional
	public ReqAddEntity addReqFolderNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		SessionUtil.setAttribute("addReqFolderNode",changeReqTableName);
		ReqAddEntity savedReqAddEntity = this.addNode(reqAddEntity);
		SessionUtil.removeAttribute("addReqFolderNode");

		return savedReqAddEntity;
	}

	@Override
	@Transactional
	public ReqAddEntity moveReqNode(ReqAddEntity reqAddEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		SessionUtil.setAttribute("moveNode", changeReqTableName);

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

		String pdServiceVersionTitles = pdServiceEntity.getPdServiceVersionEntities()
			.stream()
			.filter(a -> followReqLinkDTO.getPdServiceVersion().contains(a.getC_id()))
			.map(PdServiceVersionEntity::getC_title)
			.collect(Collectors.joining(","));

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
			.pdServiceVersion_c_title(pdServiceVersionTitles)
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
		// 1. 수정 전 ReqAdd 조회
		ResponseEntity<LoadReqAddDTO> 요구사항조회 = 내부통신기.요구사항조회(changeReqTableName, reqAddEntity.getC_id());

		LoadReqAddDTO loadReqAddDTO = 요구사항조회.getBody();

		if(loadReqAddDTO == null){
			logger.info("ReqAddImpl :: updateReqNode :: 요구사항 조회에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 수정에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		if(StringUtils.equals(loadReqAddDTO.getC_type(),TreeConstant.Branch_TYPE)){

			// 3. ReqAdd 업데이트
			SessionUtil.setAttribute("updateNode", changeReqTableName);
			this.updateNode(reqAddEntity);
			SessionUtil.removeAttribute("updateNode");

			return 1;

		}
		else if (reqAddEntity.getC_req_etc() != null && StringUtils.equals(reqAddEntity.getC_req_etc(), "칸반")) {

			// ReqAdd 업데이트
			SessionUtil.setAttribute("updateNode", changeReqTableName);

			ModelMapper modelMapper = new ModelMapper();
			ReqAddEntity loadReqAddEntity = modelMapper.map(loadReqAddDTO, ReqAddEntity.class);
			loadReqAddEntity.setReqStateEntity(reqAddEntity.getReqStateEntity());

			this.updateNode(loadReqAddEntity);
			SessionUtil.removeAttribute("updateNode");

			return 1;
		}
		else{
			String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", ""); // ex) 22

			// 2. 수정 전 후 비교
			ObjectMapper objectMapper = new ObjectMapper();
			Set<String> 수정전버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
			Set<String> 현재버전셋 = objectMapper.readValue(reqAddEntity.getC_req_pdservice_versionset_link(), Set.class);
			Set<String> 루프용버전셋 = objectMapper.readValue(loadReqAddDTO.getC_req_pdservice_versionset_link(), Set.class);
			루프용버전셋.addAll(현재버전셋);

			List<Long> 수정전버전셋리스트 = 수정전버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 1,2
			List<Long> 현재버전셋리스트 = 현재버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 2,3
			List<Long> 루프용버전셋리스트 = 루프용버전셋.stream().map(Long::valueOf).collect(Collectors.toList()); // 1,2,3

			logger.info("수정전버전셋리스트 : {}", 수정전버전셋리스트);
			logger.info("현재버전셋리스트 : {}", 현재버전셋리스트);
			logger.info("루프용버전셋리스트 : {}", 루프용버전셋리스트);

			List<GlobalTreeMapEntity> globalTreeMapEntities = globalTreeMapService.findAllByIds(루프용버전셋리스트, "pdserviceversion_link")
					.stream()
					.filter(globalTreeMap -> globalTreeMap.getJiraproject_link() != null)
					.collect(Collectors.toList());

			Set<Long> 수정전버전에연결된지라프로젝트아이디 = globalTreeMapEntities.stream()
					.filter(globalTreeMap -> 수정전버전셋리스트.contains(globalTreeMap.getPdserviceversion_link()))
					.map(GlobalTreeMapEntity::getJiraproject_link)
					.collect(Collectors.toSet());

			logger.info("수정전버전에연결된지라프로젝트아이디 : {}", 수정전버전에연결된지라프로젝트아이디);

			Set<Long> 현재버전에연결된지라프로젝트아이디 = globalTreeMapEntities.stream()
					.filter(globalTreeMap -> 현재버전셋리스트.contains(globalTreeMap.getPdserviceversion_link()))
					.map(GlobalTreeMapEntity::getJiraproject_link)
					.collect(Collectors.toSet());

			logger.info("현재버전에연결된지라프로젝트아이디 : {}", 현재버전에연결된지라프로젝트아이디);

			Set<Long> 유지된지라프로젝트아이디 = 유지된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
			Set<Long> 추가된지라프로젝트아이디 = 추가된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
			Set<Long> 삭제된지라프로젝트아이디 = 삭제된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);

			logger.info("유지된지라프로젝트아이디 : {}", 유지된지라프로젝트아이디);
			logger.info("추가된지라프로젝트아이디 : {}", 추가된지라프로젝트아이디);
			logger.info("삭제된지라프로젝트아이디 : {}", 삭제된지라프로젝트아이디);

			List<PdServiceVersionEntity> 버전데이터 = pdServiceVersion.getVersionListByCids(루프용버전셋리스트);

			List<PdServiceVersionEntity> 수정될버전 = 버전데이터.stream()
					.filter(pdServiceVersionEntity -> 현재버전셋리스트.contains(pdServiceVersionEntity.getC_id()))
					.collect(Collectors.toList());
			PdServiceEntity 제품데이터 = 제품데이터조회(pdServiceId);

			/* 지라 이슈에 사용 할 데이터 */
			String 제품명 = 제품데이터.getC_title();
			String 현재제목 = reqAddEntity.getC_title();
			String 현재본문 = reqAddEntity.getC_req_contents();

			// 3. ReqAdd 업데이트
			SessionUtil.setAttribute("updateNode", changeReqTableName);
			this.updateNode(reqAddEntity);
			SessionUtil.removeAttribute("updateNode");

			List<ReqStatusEntity> reqStatusEntityList = 내부통신기.제품별_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceId, new ReqStatusDTO());

			List<ReqStatusEntity> 유지된지라프로젝트 = reqStatusEntityList.stream()
					.filter(reqStatusEntity -> reqStatusEntity.getC_req_link().equals(reqAddEntity.getC_id()))
					.filter(reqStatusEntity -> 유지된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
					.collect(Collectors.toList());

			List<ReqStatusEntity> 삭제된지라프로젝트 = reqStatusEntityList.stream()
					.filter(reqStatusEntity -> reqStatusEntity.getC_req_link().equals(reqAddEntity.getC_id()))
					.filter(reqStatusEntity -> 삭제된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
					.collect(Collectors.toList());

			유지된지라프로젝트처리(reqAddEntity, 유지된지라프로젝트, globalTreeMapEntities, 현재제목, 현재본문, 제품명, 수정될버전, pdServiceId);

			삭제된지라프로젝트처리(reqAddEntity, 삭제된지라프로젝트, globalTreeMapEntities, 현재제목, 현재본문, 제품명, 수정될버전, pdServiceId);

			추가된지라프로젝트처리(reqAddEntity, 추가된지라프로젝트아이디, globalTreeMapEntities, 현재제목, 현재본문, 제품명, 수정될버전, reqStatusEntityList, pdServiceId);

			return 1;
		}

	}

	private void 추가된지라프로젝트처리(
			ReqAddEntity reqAddEntity,
			Set<Long> 추가된지라프로젝트아이디,
			List<GlobalTreeMapEntity> globalTreeMapEntities,
			String 현재제목,
			String 현재본문,
			String 제품명,
			List<PdServiceVersionEntity> 수정될버전,
			List<ReqStatusEntity> reqStatusEntityList,
			String pdServiceId
	) throws Exception {
		for (Long 지라_프로젝트_아이디 : 추가된지라프로젝트아이디) {
			GlobalTreeMapEntity 글로벌트리맵 = globalTreeMapService.findAllByIds(Collections.singletonList(지라_프로젝트_아이디), "jiraproject_link")
					.stream()
					.filter(globalTreeMap -> globalTreeMap.getJiraserver_link() != null)
					.findFirst().orElseThrow();

			Long 지라서버_아이디 = 글로벌트리맵.getJiraserver_link();

			List<Long> 현재지라프로젝트에연결된버전링크들 = globalTreeMapEntities.stream()
					.filter(globalTreeMapEntity -> globalTreeMapEntity.getJiraproject_link().equals(지라_프로젝트_아이디))
					.map(globalTreeMapEntity -> globalTreeMapEntity.getPdserviceversion_link())
					.collect(Collectors.toList());

			String 버전명목록 = 수정될버전.stream()
					.filter(pdServiceVersionEntity -> 현재지라프로젝트에연결된버전링크들.contains(pdServiceVersionEntity.getC_id()))
					.map(PdServiceVersionEntity::getC_title)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			String 버전아이디목록 = 현재지라프로젝트에연결된버전링크들.stream()
					.map(String::valueOf)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			String 일반지라이슈본문 = 등록및수정지라이슈본문가져오기(reqAddEntity, pdServiceId, 지라서버_아이디, 지라_프로젝트_아이디, 버전아이디목록);

			JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);
			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라_프로젝트_아이디);
			// Soft Delete된 프로젝트의 경우 이슈수정을 하지 않도록 방어 코드 추가
			if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
				continue;
			}

			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);
			JiraIssueResolutionEntity 요구사항_이슈_해결책 = 요구사항_이슈해결책검색(검색된_지라서버);

			ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());

			JiraIssueStatusEntity 요구사항_이슈_상태 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라프로젝트.getJiraIssueStatusEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라서버.getJiraIssueStatusEntities());
			}

			JiraIssueTypeEntity 요구사항_이슈타입 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈타입= 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
			}

			지라이슈필드_데이터.프로젝트 프로젝트 = 지라프로젝트빌더(검색된_지라프로젝트);

			지라이슈유형_데이터 유형;
			if (요구사항_이슈타입 != null) {
				유형 = 지라이슈유형가져오기(요구사항_이슈타입);
			}
			else {
				// 추가 생성되는 요구사항의 경우 이슈유형이 Null 일 경우 오류 처리
				logger.error(검색된_지라서버.getC_jira_server_base_url() + " 서버의 프로젝트 :"
						+ 검색된_지라프로젝트.getC_jira_name() + "에 선택된 요구사항_이슈_타입이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
				throw new Exception(검색된_지라서버.getC_jira_server_base_url() + " :: " + 검색된_지라프로젝트.getC_title() +
						" 프로젝트에 선택된 이슈유형이 없습니다. 이슈유형 기본 설정 확인이 필요합니다.");
			}

			지라이슈필드_데이터.보고자 암스서버보고자 = 암스서버보고자가져오기(검색된_지라서버);

			지라이슈필드_데이터.담당자 암스서버담당자 = 암스서버담당자가져오기(검색된_지라서버);

			지라이슈필드_데이터 지라이슈생성데이터 = 지라이슈생성데이터가져오기(reqAddEntity, 프로젝트, 유형, 일반지라이슈본문, 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책, true);

			지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
					.builder()
					.fields(지라이슈생성데이터)
					.build();

			ReqStatusDTO createReqStatus = new ReqStatusDTO();
			createReqStatus.setC_req_owner("admin");

			/* 제품 및 버전*/
			createReqStatus.setC_title(현재제목);
			createReqStatus.setC_contents(현재본문);
			createReqStatus.setC_pdservice_name(제품명);
			createReqStatus.setC_pdservice_link(Long.valueOf(pdServiceId));
			createReqStatus.setC_pds_version_name(버전명목록);
			createReqStatus.setC_req_pdservice_versionset_link(버전아이디목록); // ["33", "35"]

			/* 지라 서버 */
			createReqStatus.setC_jira_server_link(지라서버_아이디);
			createReqStatus.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
			createReqStatus.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

			/* 지라 프로젝트 */
			createReqStatus.setC_jira_project_link(지라_프로젝트_아이디);
			createReqStatus.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
			createReqStatus.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
			createReqStatus.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

			/* ReqAdd */
			createReqStatus.setC_req_link(reqAddEntity.getC_id());
			createReqStatus.setC_req_name(reqAddEntity.getC_title());

			if (요구사항_이슈_우선순위 != null) {
				createReqStatus.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
				createReqStatus.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
			}

			if (요구사항_이슈_해결책 != null) {
				createReqStatus.setC_issue_resolution_link(요구사항_이슈_해결책.getC_id());
				createReqStatus.setC_issue_resolution_name(요구사항_이슈_해결책.getC_issue_resolution_name());
			}

			if (요구사항_이슈_상태 != null) {
				createReqStatus.setC_issue_status_link(요구사항_이슈_상태.getC_id());
				createReqStatus.setC_issue_status_name(요구사항_이슈_상태.getC_issue_status_name());
			}

			createReqStatus.setC_req_plan_resource(reqAddEntity.getC_req_plan_resource());
			createReqStatus.setC_req_plan_time(reqAddEntity.getC_req_plan_time());
			createReqStatus.setC_req_total_resource(reqAddEntity.getC_req_total_resource());
			createReqStatus.setC_req_total_time(reqAddEntity.getC_req_total_time());

			if (reqAddEntity.getC_req_end_date() != null) {
				createReqStatus.setC_req_end_date(reqAddEntity.getC_req_end_date());
			}

			if (reqAddEntity.getReqPriorityEntity() != null) {
				createReqStatus.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
				createReqStatus.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
			}

			if (reqAddEntity.getReqStateEntity() != null) {
				createReqStatus.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
				createReqStatus.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
			}

			if (reqAddEntity.getReqDifficultyEntity() != null) {
				createReqStatus.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
				createReqStatus.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
			}

			Optional<ReqStatusEntity> 삭제된데이터검색 = reqStatusEntityList.stream()
					.filter(reqStatusEntity -> reqStatusEntity.getC_req_link().equals(reqAddEntity.getC_id()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() != null)
					.filter(reqStatusEntity -> reqStatusEntity.getC_jira_project_link().equals(지라_프로젝트_아이디))
					.findFirst();

			if (삭제된데이터검색.isPresent()) {
				chat.sendMessageByEngine("삭제된데이터 발견되어 이슈 수정");
				엔진통신기.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 삭제된데이터검색.get().getC_issue_key(), 요구사항_이슈);

				createReqStatus.setC_id(삭제된데이터검색.get().getC_id());
				createReqStatus.setC_issue_update_date(new Date());
				createReqStatus.setC_issue_delete_date(null);

				ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + pdServiceId, createReqStatus);

				if (결과.getStatusCode().is2xxSuccessful()) {
					chat.sendMessageByEngine("기존에 Soft Delete 처리 된 지라 이슈를 복구하였습니다. 지라 이슈는 통계에 수집됩니다.");
				}
			} else {
				chat.sendMessageByEngine("삭제된데이터가 없으므로 이슈 생성");
				지라이슈_데이터 이슈_생성하기 = 엔진통신기.이슈_생성하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), 요구사항_이슈);

				createReqStatus.setRef(TreeConstant.First_Node_CID);
				createReqStatus.setC_type(TreeConstant.Leaf_Node_TYPE);
				createReqStatus.setC_issue_key(이슈_생성하기.getKey());
				createReqStatus.setC_issue_url(이슈_생성하기.getSelf());
				createReqStatus.setC_issue_reporter(암스서버보고자.getName());
				createReqStatus.setC_issue_assignee(암스서버담당자.getName());

				// 멀티 버전으로 추가 요구사항 생성 시 reqStatus create, start date 추가 -> 기존 reqAddEntity의 create, start date를 따라갈 것인지?
				Date date = new Date();
				createReqStatus.setC_issue_create_date(date);
				createReqStatus.setC_req_start_date(date);

				ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_저장하기("T_ARMS_REQSTATUS_" + pdServiceId, createReqStatus);

				if (결과.getStatusCode().is2xxSuccessful()) {
					chat.sendMessageByEngine("지라 이슈가 등록되었습니다.");
				}
			}
		}
	}

	private void 삭제된지라프로젝트처리(
			ReqAddEntity reqAddEntity,
			List<ReqStatusEntity> 삭제된지라프로젝트,
			List<GlobalTreeMapEntity> globalTreeMapEntities,
			String 현재제목,
			String 현재본문,
			String 제품명,
			List<PdServiceVersionEntity> 수정될버전,
			String pdServiceId
	) throws Exception {
		String 삭제지라이슈본문 = 삭제할지라이슈본문가져오기();
		for (ReqStatusEntity reqStatusEntity : 삭제된지라프로젝트) {
			Long 지라서버_아이디 = reqStatusEntity.getC_jira_server_link();
			Long 지라_프로젝트_아이디 = reqStatusEntity.getC_jira_project_link();

			List<Long> 현재지라프로젝트에연결된버전링크들 = globalTreeMapEntities.stream()
					.filter(globalTreeMapEntity -> globalTreeMapEntity.getJiraproject_link().equals(지라_프로젝트_아이디))
					.map(globalTreeMapEntity -> globalTreeMapEntity.getPdserviceversion_link())
					.collect(Collectors.toList());

			String 버전명목록 = 수정될버전.stream()
					.filter(pdServiceVersionEntity -> 현재지라프로젝트에연결된버전링크들.contains(pdServiceVersionEntity.getC_id()))
					.map(PdServiceVersionEntity::getC_title)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			String 버전아이디목록 = 현재지라프로젝트에연결된버전링크들.stream()
					.map(String::valueOf)
					.collect(Collectors.joining("\",\"", "[\"", "\"]"));

			JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);
			// Soft Delete된 프로젝트의 경우 이슈수정을 하지 않도록 방어 코드 추가
			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라_프로젝트_아이디);
			if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
				continue;
			}
			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);
			JiraIssueResolutionEntity 요구사항_이슈_해결책 = 요구사항_이슈해결책검색(검색된_지라서버);

			ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());

			JiraIssueStatusEntity 요구사항_이슈_상태 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라프로젝트.getJiraIssueStatusEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라서버.getJiraIssueStatusEntities());
			}

			JiraIssueTypeEntity 요구사항_이슈_타입 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_타입= 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
			}
			else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
			}

			지라이슈필드_데이터.프로젝트 프로젝트 = 지라프로젝트빌더(검색된_지라프로젝트);

			지라이슈유형_데이터 유형 = null;
			if (요구사항_이슈_타입 != null) {
				유형 = 지라이슈유형가져오기(요구사항_이슈_타입);
			}

			지라이슈필드_데이터 지라이슈생성데이터 = 지라이슈생성데이터가져오기(reqAddEntity, 프로젝트, 유형, 삭제지라이슈본문, 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책, false);

			지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
					.builder()
					.fields(지라이슈생성데이터)
					.build();


			엔진통신기.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), reqStatusEntity.getC_issue_key(), 요구사항_이슈);

			ReqStatusDTO updateReqStatus = new ReqStatusDTO();

			/* 제품 및 버전*/
			updateReqStatus.setC_title(현재제목);
			updateReqStatus.setC_contents(현재본문);
			updateReqStatus.setC_pdservice_name(제품명);
			updateReqStatus.setC_pdservice_link(Long.valueOf(pdServiceId));
			updateReqStatus.setC_pds_version_link(reqStatusEntity.getC_pds_version_link()); // TODO: 다중 버전 지원 시 해당 필드는 deprecated 될 예정 ?
			updateReqStatus.setC_pds_version_name(버전명목록);
			updateReqStatus.setC_req_pdservice_versionset_link(버전아이디목록); // ["33", "35"]

			/* 지라 서버 */
			updateReqStatus.setC_jira_server_link(지라서버_아이디);
			updateReqStatus.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
			updateReqStatus.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

			/* 지라 프로젝트 */
			updateReqStatus.setC_jira_project_link(지라_프로젝트_아이디);
			updateReqStatus.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
			updateReqStatus.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
			updateReqStatus.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

			//-- 요구사항
			updateReqStatus.setC_req_link(reqAddEntity.getC_id());
			updateReqStatus.setC_req_name(reqAddEntity.getC_title());

			// 등록일 경우, 엔진 응답 후 처리
			updateReqStatus.setC_issue_key(reqStatusEntity.getC_issue_key());
			updateReqStatus.setC_issue_url(reqStatusEntity.getC_issue_url());

			if (요구사항_이슈_우선순위 != null) {
				updateReqStatus.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
				updateReqStatus.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
			}

			if (요구사항_이슈_해결책 != null) {
				updateReqStatus.setC_issue_resolution_link(요구사항_이슈_해결책.getC_id());
				updateReqStatus.setC_issue_resolution_name(요구사항_이슈_해결책.getC_issue_resolution_name());
			}

			if (요구사항_이슈_상태 != null) {
				updateReqStatus.setC_issue_status_link(요구사항_이슈_상태.getC_id());
				updateReqStatus.setC_issue_status_name(요구사항_이슈_상태.getC_issue_status_name());
			}

			updateReqStatus.setC_issue_update_date(new Date());
			updateReqStatus.setC_issue_delete_date(new Date());
			updateReqStatus.setC_id(reqStatusEntity.getC_id());

			updateReqStatus.setC_req_plan_resource(reqAddEntity.getC_req_plan_resource());
			updateReqStatus.setC_req_plan_time(reqAddEntity.getC_req_plan_time());
			updateReqStatus.setC_req_total_resource(reqAddEntity.getC_req_total_resource());
			updateReqStatus.setC_req_total_time(reqAddEntity.getC_req_total_time());

			if (reqAddEntity.getReqPriorityEntity() != null) {
				updateReqStatus.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
				updateReqStatus.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
			}

			if (reqAddEntity.getReqStateEntity() != null) {
				updateReqStatus.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
				updateReqStatus.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
			}

			if (reqAddEntity.getReqDifficultyEntity() != null) {
				updateReqStatus.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
				updateReqStatus.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
			}

			ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + pdServiceId, updateReqStatus);

			if (결과.getStatusCode().is2xxSuccessful()) {
				chat.sendMessageByEngine("지라 이슈가 Soft Delete 처리되었습니다. 지라 이슈는 남아있지만, 통계에 수집되지 않습니다.");
			}
		}
	}

	private void 유지된지라프로젝트처리(
			ReqAddEntity reqAddEntity,
			List<ReqStatusEntity> 유지된지라프로젝트,
			List<GlobalTreeMapEntity> globalTreeMapEntities,
			String 현재제목,
			String 현재본문,
			String 제품명,
			List<PdServiceVersionEntity> 수정될버전,
			String pdServiceId
	) throws Exception {
		for (ReqStatusEntity reqStatusEntity : 유지된지라프로젝트) {
            Long 지라서버_아이디 = reqStatusEntity.getC_jira_server_link();

            Long 지라_프로젝트_아이디 = reqStatusEntity.getC_jira_project_link();

            List<Long> 현재지라프로젝트에연결된버전링크들 = globalTreeMapEntities.stream()
                    .filter(globalTreeMapEntity -> globalTreeMapEntity.getJiraproject_link().equals(지라_프로젝트_아이디))
                    .map(globalTreeMapEntity -> globalTreeMapEntity.getPdserviceversion_link())
                    .collect(Collectors.toList());

            String 버전명목록 = 수정될버전.stream()
                    .filter(pdServiceVersionEntity -> 현재지라프로젝트에연결된버전링크들.contains(pdServiceVersionEntity.getC_id()))
                    .map(PdServiceVersionEntity::getC_title)
                    .collect(Collectors.joining("\",\"", "[\"", "\"]"));

            String 버전아이디목록 = 현재지라프로젝트에연결된버전링크들.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\",\"", "[\"", "\"]"));

            String 일반지라이슈본문 = 등록및수정지라이슈본문가져오기(reqAddEntity, pdServiceId, 지라서버_아이디, 지라_프로젝트_아이디, 버전아이디목록);

            JiraServerEntity 검색된_지라서버 = 지라서버검색(지라서버_아이디);

			JiraProjectEntity 검색된_지라프로젝트 = 지라프로젝트검색(지라_프로젝트_아이디);
			// Soft Delete된 프로젝트의 경우 이슈수정을 하지 않도록 방어 코드 추가
			if (검색된_지라프로젝트.getC_etc() != null && StringUtils.equals(검색된_지라프로젝트.getC_etc(), "delete")) {
				continue;
			}
			JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 요구사항_이슈우선순위검색(검색된_지라서버);

			JiraIssueResolutionEntity 요구사항_이슈_해결책 = 요구사항_이슈해결책검색(검색된_지라서버);

            ServerType serverType = ServerType.fromString(검색된_지라서버.getC_jira_server_type());

			JiraIssueStatusEntity 요구사항_이슈_상태 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라프로젝트.getJiraIssueStatusEntities());
			} else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_상태 = 요구사항_이슈상태검색(검색된_지라서버.getJiraIssueStatusEntities());
			}

			JiraIssueTypeEntity 요구사항_이슈_타입 = null;
			if (serverType.equals(ServerType.JIRA_CLOUD) || serverType.equals(ServerType.REDMINE_ON_PREMISE)) {
				요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라프로젝트.getJiraIssueTypeEntities());
			} else if (serverType.equals(ServerType.JIRA_ON_PREMISE)) {
				요구사항_이슈_타입 = 요구사항_이슈타입검색(검색된_지라서버.getJiraIssueTypeEntities());
			}

            지라이슈필드_데이터.프로젝트 프로젝트 = 지라프로젝트빌더(검색된_지라프로젝트);

            지라이슈유형_데이터 유형 = null;
            if (요구사항_이슈_타입 != null) {
				유형 = 지라이슈유형가져오기(요구사항_이슈_타입);
            }

            지라이슈필드_데이터 지라이슈생성데이터 = 지라이슈생성데이터가져오기(reqAddEntity, 프로젝트, 유형, 일반지라이슈본문, 요구사항_이슈_우선순위, 요구사항_이슈_상태, 요구사항_이슈_해결책, false);

            지라이슈생성_데이터 요구사항_이슈 = 지라이슈생성_데이터
                    .builder()
                    .fields(지라이슈생성데이터)
                    .build();

            엔진통신기.이슈_수정하기(Long.parseLong(검색된_지라서버.getC_jira_server_etc()), reqStatusEntity.getC_issue_key(), 요구사항_이슈);

            ReqStatusDTO updateReqStatus = new ReqStatusDTO();

            /* 제품 및 버전*/
            updateReqStatus.setC_title(현재제목);
            updateReqStatus.setC_contents(현재본문);
            updateReqStatus.setC_pdservice_name(제품명);
            updateReqStatus.setC_pdservice_link(Long.valueOf(pdServiceId));
            updateReqStatus.setC_pds_version_name(버전명목록); // ["24.05","BaseVersion"]]
            updateReqStatus.setC_req_pdservice_versionset_link(버전아이디목록); // ["33", "35"]

            /* 지라 서버 */
            updateReqStatus.setC_jira_server_link(지라서버_아이디);
            updateReqStatus.setC_jira_server_name(검색된_지라서버.getC_jira_server_name());
            updateReqStatus.setC_jira_server_url(검색된_지라서버.getC_jira_server_base_url());

            /* 지라 프로젝트 */
            updateReqStatus.setC_jira_project_link(지라_프로젝트_아이디);
            updateReqStatus.setC_jira_project_name(검색된_지라프로젝트.getC_jira_name());
            updateReqStatus.setC_jira_project_key(검색된_지라프로젝트.getC_jira_key());
            updateReqStatus.setC_jira_project_url(검색된_지라프로젝트.getC_jira_url());

            /* ReqAdd */
            updateReqStatus.setC_req_link(reqAddEntity.getC_id());
            updateReqStatus.setC_req_name(reqAddEntity.getC_title());

            /* 등록일 경우, 엔진 호출 후 처리 */
            updateReqStatus.setC_issue_key(reqStatusEntity.getC_issue_key());
            updateReqStatus.setC_issue_url(reqStatusEntity.getC_issue_url());

            if (요구사항_이슈_우선순위 != null) {
                updateReqStatus.setC_issue_priority_link(요구사항_이슈_우선순위.getC_id());
                updateReqStatus.setC_issue_priority_name(요구사항_이슈_우선순위.getC_issue_priority_name());
            }

            if (요구사항_이슈_해결책 != null) {
                updateReqStatus.setC_issue_resolution_link(요구사항_이슈_해결책.getC_id());
                updateReqStatus.setC_issue_resolution_name(요구사항_이슈_해결책.getC_issue_resolution_name());
            }

            if (요구사항_이슈_상태 != null) {
                updateReqStatus.setC_issue_status_link(요구사항_이슈_상태.getC_id());
                updateReqStatus.setC_issue_status_name(요구사항_이슈_상태.getC_issue_status_name());
            }

            updateReqStatus.setC_issue_update_date(new Date());
            updateReqStatus.setC_id(reqStatusEntity.getC_id());

            updateReqStatus.setC_req_plan_resource(reqAddEntity.getC_req_plan_resource());
            updateReqStatus.setC_req_plan_time(reqAddEntity.getC_req_plan_time());
            updateReqStatus.setC_req_total_resource(reqAddEntity.getC_req_total_resource());
            updateReqStatus.setC_req_total_time(reqAddEntity.getC_req_total_time());
            updateReqStatus.setC_req_start_date(reqAddEntity.getC_req_start_date());
            updateReqStatus.setC_req_end_date(reqAddEntity.getC_req_end_date());

            if (reqAddEntity.getReqPriorityEntity() != null) {
                updateReqStatus.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
                updateReqStatus.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
            }

            if (reqAddEntity.getReqStateEntity() != null) {
                updateReqStatus.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
                updateReqStatus.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
            }

            if (reqAddEntity.getReqDifficultyEntity() != null) {
                updateReqStatus.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
                updateReqStatus.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
            }

            ResponseEntity<?> 결과 = 내부통신기.요구사항_이슈_수정하기("T_ARMS_REQSTATUS_" + pdServiceId, updateReqStatus);

            if (결과.getStatusCode().is2xxSuccessful()) {
                chat.sendMessageByEngine("지라 이슈가 수정되었습니다.");
            }
        }
	}

	private 지라이슈유형_데이터 지라이슈유형가져오기(JiraIssueTypeEntity 요구사항_이슈_타입) {
		지라이슈유형_데이터 유형 = new 지라이슈유형_데이터();
		유형.setId(요구사항_이슈_타입.getC_issue_type_id());
		유형.setName(요구사항_이슈_타입.getC_issue_type_name());
		유형.setSelf(요구사항_이슈_타입.getC_issue_type_url());
		return 유형;
	}

	private 지라이슈필드_데이터.담당자 암스서버담당자가져오기(JiraServerEntity 검색된_지라서버) {
		지라이슈필드_데이터.담당자 암스서버담당자 = new 지라이슈필드_데이터.담당자();
		암스서버담당자.setName(검색된_지라서버.getC_jira_server_connect_id());
		암스서버담당자.setEmailAddress("313cokr@gmail.com");
		return 암스서버담당자;
	}

	private 지라이슈필드_데이터.보고자 암스서버보고자가져오기(JiraServerEntity 검색된_지라서버) {
		지라이슈필드_데이터.보고자 암스서버보고자 = new 지라이슈필드_데이터.보고자();
		암스서버보고자.setName(검색된_지라서버.getC_jira_server_connect_id());
		암스서버보고자.setEmailAddress("313cokr@gmail.com");
		return 암스서버보고자;
	}

	private 지라이슈필드_데이터 지라이슈생성데이터가져오기(
			ReqAddEntity reqAddEntity,
			지라이슈필드_데이터.프로젝트 프로젝트,
			지라이슈유형_데이터 유형, String 지라이슈본문,
			JiraIssuePriorityEntity 요구사항_이슈_우선순위,
			JiraIssueStatusEntity 요구사항_이슈_상태,
			JiraIssueResolutionEntity 요구사항_이슈_해결책,
			boolean isCreate
	) {
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
			우선순위.setDescription(요구사항_이슈_우선순위.getC_issue_priority_desc());
			지라이슈생성데이터.setPriority(우선순위);
		}

		if (요구사항_이슈_상태 != null && isCreate) {
			지라이슈상태_데이터 상태 = new 지라이슈상태_데이터();
			상태.setId(요구사항_이슈_상태.getC_issue_status_id());
			상태.setName(요구사항_이슈_상태.getC_issue_status_name());
			상태.setSelf(요구사항_이슈_상태.getC_issue_status_url());
			상태.setDescription(요구사항_이슈_상태.getC_issue_status_desc());
			지라이슈생성데이터.setStatus(상태);
		}

		if (요구사항_이슈_해결책 != null && isCreate) {
			지라이슈해결책_데이터 해결책 = new 지라이슈해결책_데이터();
			해결책.setId(요구사항_이슈_해결책.getC_issue_resolution_id());
			해결책.setName(요구사항_이슈_해결책.getC_issue_resolution_name());
			해결책.setSelf(요구사항_이슈_해결책.getC_issue_resolution_url());
			해결책.setDescription(요구사항_이슈_해결책.getC_issue_resolution_desc());
			지라이슈생성데이터.setResolution(해결책);
		}
		return 지라이슈생성데이터;
	}


	private 지라이슈필드_데이터.프로젝트 지라프로젝트빌더(JiraProjectEntity 검색된_지라프로젝트) {
		return 지라이슈필드_데이터.프로젝트.builder().id(검색된_지라프로젝트.getC_desc())
				.key(검색된_지라프로젝트.getC_jira_key())
				.name(검색된_지라프로젝트.getC_jira_name())
				.self(검색된_지라프로젝트.getC_jira_url())
				.build();
	}

	private String 등록및수정지라이슈본문가져오기(
			ReqAddEntity reqAddEntity,
			String pdServiceId,
			Long 지라서버_아이디,
			Long 지라_프로젝트_아이디,
			String 버전아이디목록
	) {
		String 지라이슈본문 = Optional.ofNullable(reqAddEntity.getC_req_contents()).orElse("지라이슈본문");

		String 추가된_요구사항의_아이디 = reqAddEntity.getC_id().toString();

		String 버전아이디목록_파싱 = 버전아이디목록.replaceAll("\\[|\\]|\"", "").replaceAll(",", ",");

		String 이슈내용 = "☀ 주의 : 본 이슈는 a-RMS에서 제공하는 요구사항 이슈 입니다.\n\n" +
				"✔ 본 이슈는 자동으로 관리되므로,\n" +
				"✔ 이슈를 강제로 삭제시 → 연결된 이슈 수집이 되지 않으므로\n" +
				"✔ 현황 통계에서 배제되어 불이익을 받을 수 있습니다.\n" +
				"✔ 아래 링크에서 요구사항을 내용을 확인 할 수 있습니다.\n\n" +
				"※ 본 이슈 하위로 Sub-Task를 만들어서 개발(업무)을 진행 하시거나, \n" +
				"※ 관련한 이슈를 연결 (LINK) 하시면, 현황 통계에 자동으로 수집됩니다.\n" +
				"――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n" +
				"자세한 요구사항 내용 확인 ⇒ http://" + armsDetailUrlConfig.getAddress() + "/arms/detail.html?page=detail&pdService=" + pdServiceId +
				"&pdServiceVersion=" + 버전아이디목록_파싱 +
				"&reqAdd=" + 추가된_요구사항의_아이디 + "&jiraServer=" + 지라서버_아이디.toString() + "&jiraProject=" + 지라_프로젝트_아이디.toString() + "\n" +
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

	private JiraIssueTypeEntity 요구사항_이슈타입검색(Set<JiraIssueTypeEntity> issueTypes) throws Exception {

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

	private JiraIssueStatusEntity 요구사항_이슈상태검색(Set<JiraIssueStatusEntity> issueStatuses) throws Exception {
		return issueStatuses.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true") && (entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst().orElse(null);
	}

	private JiraIssuePriorityEntity 요구사항_이슈우선순위검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssuePriorityEntity> 지라서버_이슈우선순위_리스트 = 지라서버.getJiraIssuePriorityEntities();
		JiraIssuePriorityEntity 요구사항_이슈_우선순위 = 지라서버_이슈우선순위_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true") && (entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst().orElse(null);
		return 요구사항_이슈_우선순위;
	}

	private JiraIssueResolutionEntity 요구사항_이슈해결책검색(JiraServerEntity 지라서버) throws Exception {
		Set<JiraIssueResolutionEntity> 지라서버_이슈해결책_리스트 = 지라서버.getJiraIssueResolutionEntities();
		JiraIssueResolutionEntity 요구사항_이슈_해결책 = 지라서버_이슈해결책_리스트.stream()
				.filter(entity -> StringUtils.equals(entity.getC_check(), "true") && (entity.getC_etc() == null || !StringUtils.equals(entity.getC_etc(), "delete")))
				.findFirst().orElse(null);
		return 요구사항_이슈_해결책;
	}

	private JiraProjectEntity 지라프로젝트검색(Long 지라_프로젝트_아이디) throws Exception {
		return TreeServiceUtils.getNode(jiraProject, 지라_프로젝트_아이디, JiraProjectEntity.class);
	}

	private JiraServerEntity 지라서버검색(Long 지라서버_아이디) throws Exception {
		return TreeServiceUtils.getNode(jiraServer, 지라서버_아이디, JiraServerEntity.class);
	}

	private Set<Long> 유지된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 유지된버전 = new HashSet<>(현재버전);
		유지된버전.retainAll(수정할버전);
		return 유지된버전;
	}

	private Set<Long> 추가된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 추가된버전 = new HashSet<>(수정할버전);
		추가된버전.removeAll(현재버전);
		return 추가된버전;
	}

	private Set<Long> 삭제된지라프로젝트찾기(Set<Long> 현재버전, Set<Long> 수정할버전) {
		Set<Long> 삭제된버전 = new HashSet<>(현재버전);
		삭제된버전.removeAll(수정할버전);
		return 삭제된버전;
	}
	private PdServiceEntity 제품데이터조회(String pdServiceId) throws Exception {
		return TreeServiceUtils.getNode(pdService, Long.valueOf(pdServiceId), PdServiceEntity.class);
	}

	@Override
	@Transactional
	public Integer updateDataBase( ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

		// reqAdd 업데이트 (상태, 우선순위, 난이도, 시작일, 종료일)
		int 요구사항_디비_업데이트_결과;
		요구사항_디비_업데이트_결과 = 요구사항_디비_업데이트(reqAddEntity,changeReqTableName);

		// reqStatus 업데이트 (상태, 우선순위, 난이도)
		int 요구사항_상태_디비_업데이트_결과;
		요구사항_상태_디비_업데이트_결과 = 요구사항_상태_디비_업데이트(reqAddEntity, changeReqTableName);

		int 요구사항_업데이트_결과 = 요구사항_디비_업데이트_결과 * 요구사항_상태_디비_업데이트_결과;

		return 요구사항_업데이트_결과;
	}

	private Integer 요구사항_디비_업데이트(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception{
		// reqAdd 업데이트
		SessionUtil.setAttribute("updateDataBase", changeReqTableName);

		int 요구사항_업데이트_결과 = this.updateNode(reqAddEntity);

		SessionUtil.removeAttribute("updateDataBase");

		if(요구사항_업데이트_결과 == 0){
			logger.info("ReqAddImpl :: updateDataBase :: 요구사항 업데이트에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 업데이트에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		return 요구사항_업데이트_결과;
	}
	private Integer 요구사항_상태_디비_업데이트(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception{

		String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");
		String 요구사항_상태_테이블 = "T_ARMS_REQSTATUS_"+pdServiceId;
		int 업데이트_결과 = 0;

		SessionUtil.setAttribute("updateDataBase", 요구사항_상태_테이블);
		ReqStatusEntity reqStatusEntity = new ReqStatusEntity();

		Criterion criterion = Restrictions.eq("c_req_link", reqAddEntity.getC_id());
		reqStatusEntity.getCriterions().add(criterion);
		List<ReqStatusEntity> 검색결과_요구사항_목록 = reqStatus.getNodesWithoutRoot(reqStatusEntity);

		for(ReqStatusEntity req : 검색결과_요구사항_목록){
			if (reqAddEntity.getReqStateEntity() != null) {
				req.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
				req.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
			}
			if (reqAddEntity.getReqPriorityEntity() != null) {
				req.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
				req.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
			}
			if (reqAddEntity.getReqDifficultyEntity() != null) {
				req.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
				req.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
			}
			업데이트_결과 = reqStatus.updateNode(req);
			업데이트_결과 *= 업데이트_결과;
		}

		SessionUtil.removeAttribute("updateDataBase");

		if(업데이트_결과 == 0){
			logger.info("ReqAddImpl :: updateDataBase :: 요구사항 업데이트에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 업데이트에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		return 업데이트_결과;
	}

	public List<요구사항_담당자> getRequirementAssignee(PdServiceEntity pdServiceEntity) throws Exception {
		List<요구사항_담당자> 요구사항_담당자_목록 = new ArrayList<>();

		요구사항_담당자_목록.addAll(집계데이터_가져오기(pdServiceEntity, IsReqType.REQUIREMENT, true));
		요구사항_담당자_목록.addAll(집계데이터_가져오기(pdServiceEntity, IsReqType.ISSUE, false));

		return 요구사항_담당자_목록;
	}

	private List<요구사항_담당자> 집계데이터_가져오기(PdServiceEntity pdServiceEntity, IsReqType isReqType, boolean isRequirement) {
		List<요구사항_담당자> 결과_목록 = new ArrayList<>();

		AggregationRequestDTO 집계요청 = new AggregationRequestDTO();
		집계요청.setPdServiceLink(pdServiceEntity.getC_id());
		집계요청.setIsReqType(isReqType);

		Optional<List<검색결과>> 집계데이터 = Optional.ofNullable(엔진통신기.제품_요구사항_담당자(집계요청).getBody());
		집계데이터.ifPresent(esData -> {
			esData.forEach(result -> {
				String 요구사항_키 = result.get필드명();
				result.get하위검색결과().get("assignees").forEach(assignee -> {
					String 담당자_아이디 = assignee.get필드명();
					String 담당자_이름 = assignee.get하위검색결과().get("displayNames").stream().findFirst().orElse(new 검색결과()).get필드명();
					String 요구사항_아이디 = assignee.get하위검색결과().get("cReqLink").stream().findFirst().orElse(new 검색결과()).get필드명();
					결과_목록.add(new 요구사항_담당자(요구사항_키, 담당자_아이디, 담당자_이름, 요구사항_아이디, isRequirement));
				});
			});
		});

		return 결과_목록;
	}
}
