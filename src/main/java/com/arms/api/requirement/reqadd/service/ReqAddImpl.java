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

import com.arms.api.analysis.common.model.AggregationRequestDTO;
import com.arms.api.analysis.common.model.IsReqType;
import com.arms.api.globaltreemap.model.GlobalTreeMapEntity;
import com.arms.api.globaltreemap.service.GlobalTreeMapService;
import com.arms.api.jira.jiraproject.model.JiraProjectEntity;
import com.arms.api.jira.jiraproject.service.JiraProject;
import com.arms.api.jira.jiraserver.model.JiraServerEntity;
import com.arms.api.jira.jiraserver.service.JiraServer;
import com.arms.api.product_service.pdservice.model.PdServiceEntity;
import com.arms.api.product_service.pdservice.service.PdService;
import com.arms.api.product_service.pdserviceversion.model.PdServiceVersionEntity;
import com.arms.api.product_service.pdserviceversion.service.PdServiceVersion;
import com.arms.api.requirement.reqadd.model.FollowReqLinkDTO;
import com.arms.api.requirement.reqadd.model.LoadReqAddDTO;
import com.arms.api.requirement.reqadd.model.ReqAddDetailDTO;
import com.arms.api.requirement.reqadd.model.ReqAddEntity;
import com.arms.api.requirement.reqadd.model.요구사항별_담당자_목록.요구사항_담당자;
import com.arms.api.requirement.reqstatus.model.CRUDType;
import com.arms.api.requirement.reqstatus.model.ReqStatusDTO;
import com.arms.api.requirement.reqstatus.model.ReqStatusEntity;
import com.arms.api.requirement.reqstatus.service.ReqStatus;
import com.arms.api.util.TreeServiceUtils;
import com.arms.api.util.communicate.external.response.aggregation.검색결과;
import com.arms.api.util.communicate.external.EngineService;
import com.arms.api.util.communicate.internal.InternalService;
import com.arms.config.ArmsDetailUrlConfig;
import com.arms.egovframework.javaservice.treeframework.TreeConstant;
import com.arms.egovframework.javaservice.treeframework.interceptor.SessionUtil;
import com.arms.egovframework.javaservice.treeframework.remote.Chat;
import com.arms.egovframework.javaservice.treeframework.service.TreeServiceImpl;
import com.arms.egovframework.javaservice.treeframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
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
public class ReqAddImpl extends TreeServiceImpl implements ReqAdd {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private EngineService EngineService;

	@Autowired
	private InternalService InternalService;

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

		// 요구사항 default 타입일 경우에만 REQSTATUS 생성 후 ALM 서버로 요구사항 이슈 생성 로직 처리
		if (StringUtils.equals(reqAddEntity.getC_type(),TreeConstant.Leaf_Node_TYPE)) {

			Long 요구사항_아이디 = savedReqAddEntity.getC_id();
			PdServiceEntity 요구사항_제품서비스 = savedReqAddEntity.getPdServiceEntity();
			Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

			ObjectMapper objectMapper = new ObjectMapper();
			String 요구사항_제품서비스_버전목록_JSON = savedReqAddEntity.getC_req_pdservice_versionset_link();
			List<String> 요구사항_제품서비스_버전목록 = Arrays.asList(objectMapper.readValue(요구사항_제품서비스_버전목록_JSON, String[].class));

			Set<Long> 지라프로젝트_아이디_셋 = new HashSet<>();
			for (String 디비에저장된_제품서비스하위_버전 : 요구사항_제품서비스_버전목록) {
				GlobalTreeMapEntity globalTreeMap = new GlobalTreeMapEntity();
				globalTreeMap.setPdserviceversion_link(Long.parseLong(디비에저장된_제품서비스하위_버전));
				List<GlobalTreeMapEntity> 버전_지라프로젝트_목록 = globalTreeMapService.findAllBy(globalTreeMap).stream()
						.filter(엔티티 -> 엔티티.getJiraproject_link() != null)
						.collect(Collectors.toList());

				for (GlobalTreeMapEntity 엔티티 : 버전_지라프로젝트_목록) {
					Long 지라프로젝트_아이디 = 엔티티.getJiraproject_link();
					지라프로젝트_아이디_셋.add(지라프로젝트_아이디);
				}
			}

			// 연결된 버전의 프로젝트 목록으로 REQSTATUS 데이터 추가
			reqStatus.추가된_프로젝트_REQSTATUS_처리(savedReqAddEntity, 지라프로젝트_아이디_셋, 요구사항_제품서비스, null);

			ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
			reqStatusDTO.setC_req_link(요구사항_아이디);

			// 생성된 REQSTATUS 데이터 목록 조회
			ResponseEntity<List<ReqStatusEntity>> 조회_결과
					= InternalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
			List<ReqStatusEntity> 요구사항_이슈_생성목록 = 조회_결과.getBody();

			// 생성된 REQSTATUS 데이터 목록을 순회하며 ALM 서버로 요구사항 이슈 생성 후 REQSTATUS 업데이트(issue key, issue url)
			Optional.ofNullable(요구사항_이슈_생성목록)
					.orElse(Collections.emptyList())
					.stream()
					.filter(요구사항_이슈 -> 요구사항_이슈.getC_etc() != null && StringUtils.equals(CRUDType.생성.getType(), 요구사항_이슈.getC_etc()))
					.forEach(요구사항_이슈_업데이트 -> reqStatus.ALM서버_요구사항_생성_또는_수정_및_REQSTATUS_업데이트(요구사항_이슈_업데이트, 제품서비스_아이디));
		}

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
	@Transactional
	public int removeReqNode(ReqAddEntity reqAddEntity, String changeReqTableName, HttpServletRequest request) throws Exception {

		ResponseEntity<LoadReqAddDTO> 요구사항조회 = InternalService.요구사항조회(changeReqTableName, reqAddEntity.getC_id());
		LoadReqAddDTO loadReqAddDTO = 요구사항조회.getBody();

		if (loadReqAddDTO == null) {
			logger.error("ReqAddImpl :: updateReqNode :: 요구사항 수정 전 데이터 조회에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 수정 전 데이터 조회에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		SessionUtil.setAttribute("removeNode", changeReqTableName);
		int removedReqAddEntity = this.removeNode(reqAddEntity);
		SessionUtil.removeAttribute("removeNode");

		// 요구사항 default 타입일 경우 REQSTATUS와 ALM 서버의 데이터 삭제 로직
		if (StringUtils.equals(loadReqAddDTO.getC_type(),TreeConstant.Leaf_Node_TYPE)) {
			// 삭제에 필요한 데이터 설정
			reqAddEntity.setC_req_pdservice_versionset_link(loadReqAddDTO.getC_req_pdservice_versionset_link());
			reqAddEntity.setC_title(loadReqAddDTO.getC_title());
			reqAddEntity.setC_req_contents(loadReqAddDTO.getC_req_contents());

			String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");
			PdServiceEntity 요구사항_제품서비스 = 제품데이터조회(pdServiceId);
			Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

			ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
			reqStatusDTO.setC_req_link(loadReqAddDTO.getC_id());

			// 삭제타입 application.yml에 설정된 값에 따라 변경, default - soft delete
			String 삭제_타입 = Optional.ofNullable(armsDetailUrlConfig.getDeleteTYpe())
										.filter(type -> !type.isEmpty()).orElse(CRUDType.소프트_삭제.getType());

			// 기존 REQSTATUS 데이터 목록 조회
			ResponseEntity<List<ReqStatusEntity>> 결과
					= InternalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceId, reqStatusDTO);
			List<ReqStatusEntity> reqStatusEntityList = 결과.getBody();

			reqStatus.유지_또는_삭제된_프로젝트_REQSTATUS_처리(reqAddEntity, reqStatusEntityList, 요구사항_제품서비스, 삭제_타입);

			// 생성된 REQSTATUS 데이터 목록 조회
			ResponseEntity<List<ReqStatusEntity>> 조회_결과
					= InternalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
			List<ReqStatusEntity> 요구사항_이슈_삭제목록 = 조회_결과.getBody();

			// 목록을 순회하며 ALM 서버로 요구사항 이슈 생성
			Optional.ofNullable(요구사항_이슈_삭제목록)
					.orElse(Collections.emptyList())
					.stream()
					.filter(요구사항_이슈 -> 요구사항_이슈.getC_etc() != null && !StringUtils.equals(CRUDType.완료.getType(), 요구사항_이슈.getC_etc()))
					.forEach(요구사항_이슈_업데이트 -> reqStatus.ALM서버_요구사항_생성_또는_수정_및_REQSTATUS_업데이트(요구사항_이슈_업데이트, 제품서비스_아이디));
		}

		return removedReqAddEntity;
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
			.reqAdd_c_req_state_link(reqAddEntity.getReqStateEntity().getC_id())
			.reqAdd_c_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id())
			.reqAdd_c_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id())
			.reqAdd_c_req_start_date(reqAddEntity.getC_req_start_date())
			.reqAdd_c_req_end_date(reqAddEntity.getC_req_end_date())
			.build();
	}

	@Override
	@Transactional
	public Integer updateReqNode(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {
		// 1. 수정 전 ReqAdd 조회
		ResponseEntity<LoadReqAddDTO> 요구사항조회 = InternalService.요구사항조회(changeReqTableName, reqAddEntity.getC_id());
		LoadReqAddDTO loadReqAddDTO = 요구사항조회.getBody();

		if (loadReqAddDTO == null) {
			logger.error("ReqAddImpl :: updateReqNode :: 요구사항 수정 전 데이터 조회에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 수정 전 데이터 조회에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		// 2. ReqAdd 업데이트
		SessionUtil.setAttribute("updateNode", changeReqTableName);
		this.updateNode(reqAddEntity);
		SessionUtil.removeAttribute("updateNode");

		// 요구사항 default 타입일 경우 REQSTATUS와 ALM 서버의 데이터 수정 로직
		if (StringUtils.equals(loadReqAddDTO.getC_type(),TreeConstant.Leaf_Node_TYPE)) {

			String pdServiceId = changeReqTableName.replace("T_ARMS_REQADD_", "");

			// 3. 수정 전 후 비교
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

			Set<Long> 추가된지라프로젝트아이디 = 추가된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
			Set<Long> 유지된지라프로젝트아이디 = 유지된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);
			Set<Long> 삭제된지라프로젝트아이디 = 삭제된지라프로젝트찾기(수정전버전에연결된지라프로젝트아이디, 현재버전에연결된지라프로젝트아이디);

			logger.info("유지된지라프로젝트아이디 : {}", 유지된지라프로젝트아이디);
			logger.info("추가된지라프로젝트아이디 : {}", 추가된지라프로젝트아이디);
			logger.info("삭제된지라프로젝트아이디 : {}", 삭제된지라프로젝트아이디);

			PdServiceEntity 요구사항_제품서비스 = 제품데이터조회(pdServiceId);
			Long 제품서비스_아이디 = 요구사항_제품서비스.getC_id();

			ReqStatusDTO reqStatusDTO = new ReqStatusDTO();
			reqStatusDTO.setC_req_link(reqAddEntity.getC_id());

			// 기존 REQSTATUS 데이터 목록 조회
			ResponseEntity<List<ReqStatusEntity>> 결과
					= InternalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + pdServiceId, reqStatusDTO);
			List<ReqStatusEntity> reqStatusEntityList = 결과.getBody();

			List<ReqStatusEntity> 유지된지라프로젝트 = Optional.ofNullable(reqStatusEntityList)
					.orElse(Collections.emptyList())
					.stream()
					.filter(reqStatusEntity -> 유지된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
					.collect(Collectors.toList());

			List<ReqStatusEntity> 삭제된지라프로젝트 = Optional.ofNullable(reqStatusEntityList)
					.orElse(Collections.emptyList())
					.stream()
					.filter(reqStatusEntity -> 삭제된지라프로젝트아이디.contains(reqStatusEntity.getC_jira_project_link()))
					.filter(reqStatusEntity -> reqStatusEntity.getC_issue_delete_date() == null)
					.collect(Collectors.toList());

			reqStatus.추가된_프로젝트_REQSTATUS_처리(reqAddEntity, 추가된지라프로젝트아이디, 요구사항_제품서비스, reqStatusEntityList);
			reqStatus.유지_또는_삭제된_프로젝트_REQSTATUS_처리(reqAddEntity, 유지된지라프로젝트, 요구사항_제품서비스, CRUDType.수정.getType());
			// 요구사항 수정 시 연결 해제된 프로젝트의 요구사항 이슈의 경우 soft delete 처리
			reqStatus.유지_또는_삭제된_프로젝트_REQSTATUS_처리(reqAddEntity, 삭제된지라프로젝트, 요구사항_제품서비스, CRUDType.소프트_삭제.getType());

			// 생성된 REQSTATUS 데이터 목록 조회
			ResponseEntity<List<ReqStatusEntity>> 조회_결과
					= InternalService.REQADD_CID_요구사항_이슈_조회("T_ARMS_REQSTATUS_" + 제품서비스_아이디, reqStatusDTO);
			List<ReqStatusEntity> 요구사항_이슈_수정목록 = 조회_결과.getBody();

			// 목록을 순회하며 ALM 서버로 요구사항 이슈 생성
			Optional.ofNullable(요구사항_이슈_수정목록)
					.orElse(Collections.emptyList())
					.stream()
					.filter(요구사항_이슈 -> 요구사항_이슈.getC_etc() != null && !StringUtils.equals(CRUDType.완료.getType(), 요구사항_이슈.getC_etc()))
					.forEach(요구사항_이슈_업데이트 -> reqStatus.ALM서버_요구사항_생성_또는_수정_및_REQSTATUS_업데이트(요구사항_이슈_업데이트, 제품서비스_아이디));
		}

		return 1;
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
	public Integer updateDataBase(ReqAddEntity reqAddEntity, String changeReqTableName) throws Exception {

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

		if (요구사항_업데이트_결과 == 0) {
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
		ReqStatusEntity searchEntity = new ReqStatusEntity();

		Criterion criterion = Restrictions.eq("c_req_link", reqAddEntity.getC_id());
		searchEntity.getCriterions().add(criterion);
		List<ReqStatusEntity> reqStatusEntityList = reqStatus.getNodesWithoutRoot(searchEntity);

		for(ReqStatusEntity reqStatusEntity : reqStatusEntityList){
			if (reqAddEntity.getReqStateEntity() != null) {
				reqStatusEntity.setC_req_state_name(reqAddEntity.getReqStateEntity().getC_title());
				reqStatusEntity.setC_req_state_link(reqAddEntity.getReqStateEntity().getC_id());
			}

			if (reqAddEntity.getReqPriorityEntity() != null) {
				reqStatusEntity.setC_req_priority_name(reqAddEntity.getReqPriorityEntity().getC_title());
				reqStatusEntity.setC_req_priority_link(reqAddEntity.getReqPriorityEntity().getC_id());
			}

			if (reqAddEntity.getReqDifficultyEntity() != null) {
				reqStatusEntity.setC_req_difficulty_name(reqAddEntity.getReqDifficultyEntity().getC_title());
				reqStatusEntity.setC_req_difficulty_link(reqAddEntity.getReqDifficultyEntity().getC_id());
			}

			// ALM 요구사항 상태 변경 요청 (상태 값 변경이 있을 경우)
			reqStatus.ALM_이슈상태_업데이트(reqStatusEntity);

			업데이트_결과 = reqStatus.updateNode(reqStatusEntity);
			업데이트_결과 *= 업데이트_결과;

		}

		SessionUtil.removeAttribute("updateDataBase");

		if (업데이트_결과 == 0) {
			logger.info("ReqAddImpl :: updateDataBase :: 요구사항 업데이트에 실패했습니다. 요구사항 ID : " + reqAddEntity.getC_id());
			throw new Exception("요구사항 업데이트에 실패했습니다. 관리자에게 문의해 주세요.");
		}

		return 업데이트_결과;
	}

	public List<요구사항_담당자> getRequirementAssignee(PdServiceEntity pdServiceEntity)  {
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

		Optional<List<검색결과>> 집계데이터 = Optional.ofNullable(EngineService.제품_요구사항_담당자(집계요청).getBody());
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
